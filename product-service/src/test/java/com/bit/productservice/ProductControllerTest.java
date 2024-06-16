package com.bit.productservice;

import com.bit.productservice.controller.ProductController;
import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.dto.ProductResponse;
import com.bit.productservice.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(new ProductController(productService)).build();
    }

    @Test
    void getProduct_shouldReturnProduct() throws Exception {
        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(1L);

        when(productService.getProduct(anyLong())).thenReturn(productResponse);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getAllProducts_shouldReturnProductList() throws Exception {
        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(1L);

        when(productService.getAllProducts()).thenReturn(Collections.singletonList(productResponse));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getDeletedProducts_shouldReturnDeletedProductList() throws Exception {
        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(1L);

        when(productService.getDeletedProducts()).thenReturn(Collections.singletonList(productResponse));

        mockMvc.perform(get("/products/deleted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getAllProductsFilteredAndSorted_shouldReturnPagedProducts() throws Exception {
        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(1L);

        Page<ProductResponse> pagedResponse = new PageImpl<>(Collections.singletonList(productResponse));

        when(productService.getAllProductsFilteredAndSorted(anyInt(), anyInt(), anyString(), anyString(), any(), any(), any(), any())).thenReturn(pagedResponse);

        mockMvc.perform(get("/products/filteredAndSorted")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    void createProduct_shouldReturnCreatedProduct() throws Exception {
        ProductRequest productRequest = new ProductRequest();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(1L);

        when(productService.createProduct(any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Product\", \"description\": \"Description\", \"price\": 100.0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void updateProduct_shouldReturnUpdatedProduct() throws Exception {
        ProductRequest productRequest = new ProductRequest();
        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(1L);

        when(productService.updateProduct(anyLong(), any(ProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Product\", \"description\": \"Description\", \"price\": 100.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void restoreProduct_shouldReturnRestoredProduct() throws Exception {
        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(1L);

        when(productService.restoreProduct(anyLong())).thenReturn(productResponse);

        mockMvc.perform(put("/products/restore/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void deleteProduct_shouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Product soft deleted successfully!"));
    }

    @Test
    void deleteProductPermanently_shouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(delete("/products/permanent/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Product deleted permanently!"));
    }

    @Test
    void checkStock_shouldReturnStockStatus() throws Exception {
        when(productService.checkStock(anyLong(), anyInt())).thenReturn(true);

        mockMvc.perform(post("/products/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 1, \"requestedQuantity\": 10}"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void returnProducts_shouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(post("/products/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\": 1, \"returnedQuantity\": 10}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Product return request processed successfully."));
    }
}
