package kr.hhplus.be.server.domain.product;

import kr.hhplus.be.server.common.exception.BusinessError;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.infrastructure.product.ProductQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 상품 ID를 기반으로 상품 상세 정보를 조회합니다.
     *
     * @param productId 조회할 상품의 ID
     * @return 상품 상세 정보
     * @throws BusinessException 상품, 옵션, 재고가 존재하지 않을 경우 예외 발생
     */
    @Transactional(readOnly = true)
    public ProductInfo.Detail getProductById(long productId) {
        ProductQuery.DetailProjection detailProjection = productRepository.findProductDetailById(productId)
                .orElseThrow(() -> new BusinessException(BusinessError.PRODUCT_NOT_FOUND));

        List<ProductQuery.OptionProjection> optionProjections = productRepository.findProductOptionsByProductId(detailProjection.getProductId());

        List<ProductInfo.Option> options = optionProjections.stream()
                .map(ProductInfo.Option::from)
                .toList();

        return ProductInfo.Detail.from(detailProjection, options);
    }

    @Transactional(readOnly = true)
    public List<ProductInfo.PriceOption> getProductOptionsById(ProductCommand.OptionIds command) {
        List<Long> optionIds = command.getOptionIds();

        return productRepository.findProductOptionsById(optionIds).stream()
                .map(ProductInfo.PriceOption::from)
                .toList();
    }

    /**
     * 상품 옵션의 재고 수량을 증가시킵니다.
     *
     * 1. 요청받은 옵션 ID 리스트로 재고 정보 조회
     * 2. 요청 수량 유효성 검사 및 재고 존재 여부 확인
     * 3. 각 옵션별로 재고 수량 증가 처리
     * 4. 변경된 재고 정보 저장 후 결과 반환
     *
     * @param command 재고 증가 요청 정보 (옵션 ID, 증가할 수량 포함)
     * @return 증가된 재고 정보를 포함한 결과 DTO
     * @throws BusinessException 재고 수량이 0 이하이거나, 재고 정보가 존재하지 않을 경우 예외 발생
     */
    @Transactional
    public ProductInfo.IncreaseStock increaseStockQuantity(ProductCommand.IncreaseStock command) {
        List<ProductCommand.OptionStock> optionStocks = command.getOptionStocks();

        if (CollectionUtils.isEmpty(optionStocks)) {
            throw new BusinessException(BusinessError.STOCK_OPERATION_EMPTY);
        }

        List<Long> optionIds = optionStocks.stream().map(ProductCommand.OptionStock::getOptionId).distinct().toList();
        List<Stock> stocks = productRepository.findStocksByOptionId(optionIds);

        if (CollectionUtils.isEmpty(stocks) || stocks.size() != optionIds.size()) {
            throw new BusinessException(BusinessError.STOCK_NOT_FOUND);
        }

        for (int i = 0; i < optionIds.size(); i++) {
            ProductCommand.OptionStock optionStock = optionStocks.get(i);
            Stock stock = stocks.get(i);

            long desiredQuantity = optionStock.getQuantity();
            stock.increase(desiredQuantity);
        }

        productRepository.saveStocks(stocks);

        List<ProductInfo.OptionStock> results = stocks.stream()
                .map(stock -> ProductInfo.OptionStock.builder()
                        .stockId(stock.getId())
                        .optionId(stock.getProductOptionId())
                        .quantity(stock.getQuantity())
                        .build()
                )
                .toList();

        return ProductInfo.IncreaseStock.builder()
                .optionStocks(results)
                .build();
    }

    /**
     * 상품 옵션의 재고 수량을 차감합니다.
     *
     * 1. 요청받은 옵션 ID 리스트로 재고 정보 조회
     * 2. 요청 수량 유효성 검사 및 재고 존재 여부 확인
     * 3. 각 옵션별로 재고 수량 차감 처리 (재고 부족 시 예외 발생)
     * 4. 변경된 재고 정보 저장 후 결과 반환
     *
     * @param command 재고 차감 요청 정보 (옵션 ID, 차감할 수량 포함)
     * @return 차감된 재고 정보를 포함한 결과 DTO
     * @throws BusinessException 재고 정보가 없거나 차감하려는 수량이 현재 재고보다 많을 경우 발생
     */
    @Transactional
    public ProductInfo.DecreaseStock decreaseStockQuantity(ProductCommand.DecreaseStock command) {
        List<ProductCommand.OptionStock> optionStocks = command.getOptionStocks();

        if (CollectionUtils.isEmpty(optionStocks)) {
            throw new BusinessException(BusinessError.STOCK_OPERATION_EMPTY);
        }

        List<Long> optionIds = optionStocks.stream().map(ProductCommand.OptionStock::getOptionId).distinct().toList();
        List<Stock> stocks = productRepository.findStocksByOptionId(optionIds);

        if (CollectionUtils.isEmpty(stocks) || stocks.size() != optionIds.size()) {
            throw new BusinessException(BusinessError.PRODUCT_STOCK_NOT_FOUND);
        }

        for (int i = 0; i < optionIds.size(); i++) {
            ProductCommand.OptionStock optionStock = optionStocks.get(i);
            Stock stock = stocks.get(i);

            long desiredQuantity = optionStock.getQuantity();
            Long savedQuantity = stock.getQuantity();

            // 재고가 음수가 될 수 없다.
            if (savedQuantity < desiredQuantity) {
                throw new BusinessException(BusinessError.STOCK_QUANTITY_EXCEEDED);
            }

            stock.decrease(desiredQuantity);
        }

        productRepository.saveStocks(stocks);

        List<ProductInfo.OptionStock> results = stocks.stream()
                .map(stock -> ProductInfo.OptionStock.builder()
                        .stockId(stock.getId())
                        .optionId(stock.getProductOptionId())
                        .quantity(stock.getQuantity())
                        .build()
                )
                .toList();

        return ProductInfo.DecreaseStock.builder()
                .optionStocks(results)
                .build();
    }

    // 인기 상품 조회
    @Transactional(readOnly = true)
    public List<ProductInfo.TopSelling> getTopSellingProducts() {
        return productRepository.findTopSellingProducts().stream()
                .map(ProductInfo.TopSelling::from)
                .toList();
    }

}
