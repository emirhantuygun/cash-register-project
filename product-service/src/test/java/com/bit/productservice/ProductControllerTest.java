package com.bit.productservice;

import com.bit.productservice.controller.ProductController;
import com.bit.productservice.dto.ProductResponse;
import com.bit.productservice.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @MockBean
    private ProductService productService;
    private MockMvc mockMvc;
    private ProductResponse mockProductResponse;

    @BeforeEach
    public void setUp() {
        ProductController productController = new ProductController(productService);
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();

        mockProductResponse = ProductResponse.builder()
                .id(10L)
                .name("Galaxy A5")
                .description("a phone")
                .price(new BigDecimal(500))
                .build();
    }

    @AfterEach
    public void tearDown() {
        // Perform cleanup tasks here
    }

    @Test
    public void shouldReturnProductByIdWhenIdExists() throws Exception {
        Mockito.when(productService.getProduct(10L)).thenReturn(mockProductResponse);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/products/{id}", 10L))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Galaxy A5"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("a phone"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price").value(500));
    }
}
