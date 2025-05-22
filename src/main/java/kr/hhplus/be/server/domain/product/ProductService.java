package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class ProductService {

    private static final String CACHE_NAME = "topSellingProducts";

    private final ProductRepository productRepository;

    private final ProductRedisRepository productRedisRepository;

    /**
     * 상품 ID를 기반으로 상품 상세 정보를 조회합니다.
     *
     * @param productId 조회할 상품의 ID
     * @return 상품 상세 정보
     * @throws BusinessException 상품, 옵션, 재고가 존재하지 않을 경우 예외 발생
     */
    @Transactional(readOnly = true)
    public ProductInfo.Detail getProductById(long productId) {
        // 1. 상품 ID로 상품 상세 정보를 조회
        Product detail = productRepository.findProductById(productId)
                .orElseThrow(() -> new BusinessException(BusinessError.PRODUCT_NOT_FOUND));

        // 2. 해당 상품의 옵션 목록을 조회
        List<ProductQuery.Option> optionProjections = productRepository.findProductOptionsByProductId(detail.getId());

        // 3. 옵션 projection을 response DTO로 변환
        List<ProductInfo.Option> options = optionProjections.stream()
                .map(ProductQuery.Option::to)  // projection → DTO
                .toList();

        // 4. 상품 상세 정보와 옵션 목록을 합쳐 최종 응답 DTO 생성
        return ProductInfo.Detail.from(detail, options);
    }

    @Transactional(readOnly = true)
    public List<ProductInfo.PriceOption> getProductOptionsById(ProductCommand.OptionIds command) {
        List<Long> optionIds = command.getOptionIds();

        List<ProductQuery.PriceOption> options = productRepository.findProductOptionsById(optionIds);

        if (options.isEmpty()) {
            throw new BusinessException(BusinessError.PRODUCT_NOT_FOUND);
        }

        return options.stream()
                .map(ProductQuery.PriceOption::to)
                .toList();
    }

    /**
     * 상품 옵션의 재고 수량을 증가시킵니다.
     *
     * @param command 재고 증가 요청 정보 (옵션 ID, 증가할 수량 포함)
     * @return 증가된 재고 정보를 포함한 결과 DTO
     * @throws BusinessException 재고 수량이 0 이하이거나, 재고 정보가 존재하지 않을 경우 예외 발생
     */
    @Transactional
    public ProductInfo.IncreaseStock increaseStockQuantity(ProductCommand.IncreaseStock command) {
        // 클라이언트가 전달한 옵션 ID와 수량 정보 리스트를 추출
        List<ProductCommand.OptionStock> optionStocks = command.getOptionStocks();

        // 옵션이 비어있을 경우 예외 발생
        if (CollectionUtils.isEmpty(optionStocks)) {
            throw new BusinessException(BusinessError.STOCK_OPERATION_EMPTY);
        }

        // 중복 제거한 옵션 ID 리스트 생성
        List<Long> optionIds = optionStocks.stream().map(ProductCommand.OptionStock::getOptionId).distinct().toList();

        // 옵션 ID에 해당하는 재고 엔티티를 DB에서 조회
        List<Stock> stocks = productRepository.findStocksByOptionId(optionIds);

        // 재고가 없거나 일부 옵션의 재고만 조회된 경우 예외 발생
        if (CollectionUtils.isEmpty(stocks) || stocks.size() != optionIds.size()) {
            throw new BusinessException(BusinessError.STOCK_NOT_FOUND);
        }

        // 각 옵션별로 증가시킬 수량만큼 재고 객체에 반영
        for (int i = 0; i < optionIds.size(); i++) {
            ProductCommand.OptionStock optionStock = optionStocks.get(i); // 요청한 옵션 수량 정보
            Stock stock = stocks.get(i); // 해당 옵션의 재고 엔티티

            long desiredQuantity = optionStock.getQuantity(); // 증가시킬 수량
            stock.increase(desiredQuantity); // 재고 증가 로직 수행
        }

        // 증가된 재고들을 저장 (DB 반영)
        productRepository.saveStocks(stocks);

        // 응답용 DTO 리스트로 변환
        List<ProductInfo.OptionStock> results = stocks.stream()
                .map(ProductInfo.OptionStock::from)
                .toList();

        // 최종 응답 객체 생성 후 반환
        return ProductInfo.IncreaseStock.builder()
                .optionStocks(results)
                .build();
    }

    /**
     * 상품 옵션의 재고 수량을 차감합니다.
     *
     * @param command 재고 차감 요청 정보 (옵션 ID, 차감할 수량 포함)
     * @return 차감된 재고 정보를 포함한 결과 DTO
     * @throws BusinessException 재고 정보가 없거나 차감하려는 수량이 현재 재고보다 많을 경우 발생
     */
    @Transactional
    public ProductInfo.DecreaseStock decreaseStockQuantity(ProductCommand.DecreaseStock command) {
        // 요청된 옵션 ID 및 수량 리스트를 추출
        List<ProductCommand.OptionStock> optionStocks = command.getOptionStocks();

        // 옵션이 비어 있으면 예외 발생 (입력값 검증)
        if (CollectionUtils.isEmpty(optionStocks)) {
            throw new BusinessException(BusinessError.STOCK_OPERATION_EMPTY);
        }

        // 중복 제거한 옵션 ID 리스트 생성
        List<Long> optionIds = optionStocks.stream()
                .map(ProductCommand.OptionStock::getOptionId)
                .distinct()
                .toList();

        // DB에서 옵션 ID에 해당하는 재고 리스트 조회
        List<Stock> stocks = productRepository.findByProductOptionIdInWithLock(optionIds);

        // 재고가 없거나 개수가 일치하지 않으면 예외 발생 (재고 조회 실패)
        if (CollectionUtils.isEmpty(stocks) || stocks.size() != optionIds.size()) {
            throw new BusinessException(BusinessError.PRODUCT_STOCK_NOT_FOUND);
        }

        // 각 옵션에 대해 차감 가능 여부 확인 및 차감 수행
        for (int i = 0; i < optionIds.size(); i++) {
            ProductCommand.OptionStock optionStock = optionStocks.get(i);
            Stock stock = stocks.get(i);

            long desiredQuantity = optionStock.getQuantity();  // 요청 수량
            Long savedQuantity = stock.getQuantity();          // 현재 재고 수량

            // 요청 수량이 현재 재고보다 많으면 예외 발생
            if (savedQuantity < desiredQuantity) {
                throw new BusinessException(BusinessError.STOCK_QUANTITY_EXCEEDED);
            }

            // 재고 차감
            stock.decrease(desiredQuantity);
        }

        // 변경된 재고 저장
        productRepository.saveStocks(stocks);

        // 응답을 위한 DTO로 변환
        List<ProductInfo.OptionStock> results = stocks.stream()
                .map(ProductInfo.OptionStock::from)
                .toList();

        // 응답 객체 생성 및 반환
        return ProductInfo.DecreaseStock.builder()
                .optionStocks(results)
                .build();
    }

    // 인기 상품 조회
    @Transactional(readOnly = true)
    public ProductInfo.ProductSalesData getTopSellingProducts(LocalDate daysAgo, long limit) {

        // 캐시에서 인기 상품 조회
        Set<ZSetOperations.TypedTuple<String>> cachedProductIds = productRedisRepository.getAllTopSellingProductsFromCache();

        List<ProductInfo.TopSelling> topSelling = new ArrayList<>();

        if (!CollectionUtils.isEmpty(cachedProductIds)) {
            for (ZSetOperations.TypedTuple<String> entry : cachedProductIds) {
                // 상품 ID와 판매 수량(score)을 가져옵니다.
                String productId = entry.getValue();
                Long salesCount = entry.getScore().longValue();  // 판매 수량(score)

                // 상품 ID로 DB에서 상품 정보를 조회
                Product productById = productRepository.findProductById(Long.parseLong(productId))
                        .orElseThrow(() -> new BusinessException(BusinessError.PRODUCT_NOT_FOUND));

                // 판매 수량(salesCount)과 함께 TopSelling 객체를 생성하여 리스트에 추가
                topSelling.add(ProductInfo.TopSelling.from(Long.parseLong(productId), productById.getName(), salesCount));
            }

            return ProductInfo.ProductSalesData.builder()
                    .list(topSelling)
                    .build();
        }
        topSelling = productRepository.findTopSellingProducts(daysAgo, limit).stream()
                .map(ProductQuery.TopSelling::to)
                .toList();

        return ProductInfo.ProductSalesData.builder()
                .list(topSelling)
                .build();
    }

    public void refreshTopSellingProductsCache(LocalDate daysAgo, long limit) {

        // 상품 판매 데이터를 DB에서 가져옵니다.
        List<ProductQuery.TopSelling> topSellingProducts = productRepository.findTopSellingProducts(daysAgo, limit);

        // Redis Sorted Set에 상품 데이터를 저장
        topSellingProducts.forEach(productSignal -> {
            productRedisRepository.addTopSellingProductToCache(productSignal.productId(), productSignal.salesCount());
        });
    }

    @Transactional
    public void storeProductSignal(List<ProductCommand.ProductSignal> productSignalList) {

        for (ProductCommand.ProductSignal productSignal : productSignalList) {
            Optional<ProductSignal> signal = productRepository.findProductSignalByDateAndProductId(
                    productSignal.getDate(),
                    productSignal.getProductId()
            );

            signal.ifPresentOrElse(
                    existingProductSignal -> {
                        existingProductSignal.incrementOrderCount(productSignal.getQuantity());
                    },
                    () -> {
                        // 조회 결과가 없으면 저장
                        ProductSignal newProductSignal = ProductSignal.builder()
                                .productId(productSignal.getProductId())
                                .date(productSignal.getDate())
                                .name(productSignal.getName())
                                .orderCount(productSignal.getQuantity())
                                .build();

                        productRepository.saveProductSignal(newProductSignal);
                    }
            );

        }

    }

}
