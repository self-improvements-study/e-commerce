package kr.hhplus.be.server.api.product.presentation.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("상품 조회")
    void findProduct() throws Exception {
        // given
        long productId = 1L;

        mockMvc
                // when
                .perform(get("/api/v1/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(200)))
                .andExpect(jsonPath("$.message", equalTo("성공")))
                .andExpect(jsonPath("$.data.productId", equalTo(1)))
                .andExpect(jsonPath("$.data.name", equalTo("운동화")))
                .andExpect(jsonPath("$.data.price", equalTo(99000)))
                .andExpect(jsonPath("$.data.options", notNullValue()));
    }

    @Test
    @DisplayName("판매 우수 상품 조회")
    void findTopSellingProducts() throws Exception {
        mockMvc
                // when
                .perform(get("/api/v1/products/top-sellers")
                        .contentType(MediaType.APPLICATION_JSON))

                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", equalTo(200)))
                .andExpect(jsonPath("$.message", equalTo("성공")))
                .andExpect(jsonPath("$.data[0].productId", equalTo(1)))
                .andExpect(jsonPath("$.data[0].name", equalTo("스마트폰")))
                .andExpect(jsonPath("$.data[1].productId", equalTo(2)))
                .andExpect(jsonPath("$.data[1].name", equalTo("태블릿")));
    }
}
