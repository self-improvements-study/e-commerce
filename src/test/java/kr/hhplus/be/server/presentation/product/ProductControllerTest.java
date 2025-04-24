package kr.hhplus.be.server.presentation.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.application.product.ProductFacade;
import kr.hhplus.be.server.application.product.ProductResult;
import kr.hhplus.be.server.common.exception.BusinessException;
import kr.hhplus.be.server.common.web.ExceptionTranslator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductFacade productFacade;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExceptionTranslator<BusinessException> exceptionTranslator;

    @Test
    @DisplayName("상품 조회 성공")
    void findProduct() throws Exception {
        // given
        Long productId = 1L;
        ProductResult.Detail productDetail = ProductResult.Detail.builder()
                .productId(productId)
                .name("Product 1")
                .price(1000L)
                .options(List.of(
                        ProductResult.Option.builder()
                                .optionId(1L)
                                .size("M")
                                .color("Red")
                                .stock(50L)
                                .build()
                ))
                .build();

        when(productFacade.getProduct(productId)).thenReturn(productDetail);

        // when & then
        mockMvc.perform(get("/api/v1/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.productId").value(productId))
                .andExpect(jsonPath("$.data.name").value("Product 1"))
                .andExpect(jsonPath("$.data.price").value(1000))
                .andExpect(jsonPath("$.data.options[0].optionId").value(1))
                .andExpect(jsonPath("$.data.options[0].size").value("M"))
                .andExpect(jsonPath("$.data.options[0].color").value("Red"))
                .andExpect(jsonPath("$.data.options[0].stock").value(50));
    }

    @Test
    @DisplayName("판매 우수 상품 조회 성공")
    void findTopSellingProducts() throws Exception {
        // given
        List<ProductResult.TopSelling> topSellingProducts = List.of(
                ProductResult.TopSelling.builder()
                        .productId(1L)
                        .name("Product 1")
                        .salesCount(100)
                        .build(),
                ProductResult.TopSelling.builder()
                        .productId(2L)
                        .name("Product 2")
                        .salesCount(200)
                        .build()
        );

        when(productFacade.findTopSellingProducts()).thenReturn(topSellingProducts);

        // when & then
        mockMvc.perform(get("/api/v1/products/top-sellers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data[0].productId").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Product 1"))
                .andExpect(jsonPath("$.data[0].salesCount").value(100))
                .andExpect(jsonPath("$.data[1].productId").value(2))
                .andExpect(jsonPath("$.data[1].name").value("Product 2"))
                .andExpect(jsonPath("$.data[1].salesCount").value(200));
    }
}
