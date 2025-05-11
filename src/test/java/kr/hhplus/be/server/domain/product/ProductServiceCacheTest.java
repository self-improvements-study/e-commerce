package kr.hhplus.be.server.domain.product;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("상품 캐시 테스트")
class ProductServiceCacheTest {

    @Autowired
    ProductService sut;

    @MockitoBean
    ProductRepository productRepository;

    @Autowired
    CacheManager cacheManager;

    @Nested
    @DisplayName("인기상품 조회 데이터가 캐시에 존재하면")
    class GetTopSellingProducts {

        @Test
        @DisplayName("레디스에서 조회 해야한다")
        void success1() {
            // given
            List<ProductQuery.TopSelling> items = List.of(
                    new ProductQuery.TopSelling(1L, "1", 1L),
                    new ProductQuery.TopSelling(2L, "1", 1L),
                    new ProductQuery.TopSelling(3L, "1", 1L),
                    new ProductQuery.TopSelling(4L, "1", 1L),
                    new ProductQuery.TopSelling(5L, "1", 1L)
            );

            LocalDate daysAgo = LocalDate.now().minusDays(3);
            long limit = 3L;
            when(productRepository.findTopSellingProducts(eq(daysAgo), eq(limit)))
                    .thenReturn(items);
            sut.refreshTopSellingProductsCache(daysAgo, limit);

            ProductService productService = spy(sut);

            // when
            productService.getTopSellingProducts(daysAgo, limit);

            // then
            verify(productRepository, times(1))
                    .findTopSellingProducts(eq(daysAgo), eq(limit));
        }

    }

    @Nested
    @DisplayName("새로운 인기상품 조회 데이터를 캐시로 갱신하면")
    class RefreshTopSellingProductsCache {

        @Test
        @DisplayName("레디스에 적재되어야 한다")
        void success1() {

            // given
            List<ProductQuery.TopSelling> items = List.of(
                    new ProductQuery.TopSelling(1L, "1", 1L),
                    new ProductQuery.TopSelling(2L, "1", 1L),
                    new ProductQuery.TopSelling(3L, "1", 1L),
                    new ProductQuery.TopSelling(4L, "1", 1L),
                    new ProductQuery.TopSelling(5L, "1", 1L)
            );

            LocalDate daysAgo = LocalDate.now().minusDays(3);
            long limit = 3L;
            when(productRepository.findTopSellingProducts(eq(daysAgo), eq(limit))).thenReturn(items);

            // when
            sut.refreshTopSellingProductsCache(daysAgo, limit);

            // then
            String cacheKey = daysAgo.toString().replace("-", "");
            Cache cache = cacheManager.getCache("topSellingProducts");

            assertThat(cache)
                    .isNotNull()
                    .extracting(it -> ProductInfo.ProductSalesData.class.cast(it.get(cacheKey).get()))
                    .isNotNull()
                    .extracting(ProductInfo.ProductSalesData::getList, InstanceOfAssertFactories.LIST)
                    .hasSameSizeAs(items);
        }

    }

}
