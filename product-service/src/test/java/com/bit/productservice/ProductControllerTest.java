package com.bit.productservice;
import com.bit.productservice.controller.ProductController;
import com.bit.productservice.dto.ProductRequest;
import com.bit.productservice.dto.ProductResponse;
import com.bit.productservice.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    @DisplayName("getProduct - Should Return Product By Id When Id Exists")
    public void shouldReturnProductById_WhenIdExists() throws Exception {
        long productId = 10L;
        ProductResponse productResponse = ProductResponse.builder()
                .id(productId)
                .name("Galaxy A5")
                .description("a phone")
                .price(BigDecimal.valueOf(500))
                .build();

        when(productService.getProduct(productId)).thenReturn(productResponse);

        mockMvc.perform(get("/api/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Galaxy A5"))
                .andExpect(jsonPath("$.description").value("a phone"))
                .andExpect(jsonPath("$.price").value(500));

        verify(productService, times(1)).getProduct(productId);
    }

    @Test
    @DisplayName("getAllProducts - Should Return All Products")
    public void shouldReturnAllProducts() throws Exception {
        List<ProductResponse> productResponses = Arrays.asList(
                ProductResponse.builder().id(1L).name("Product 1").build(),
                ProductResponse.builder().id(2L).name("Product 2").build()
        );

        when(productService.getAllProducts()).thenReturn(productResponses);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Product 2"));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    @DisplayName("getDeletedProducts - Should Return Deleted Products")
    public void shouldReturnDeletedProducts() throws Exception {
        // Mocked list of deleted products
        List<ProductResponse> deletedProducts = Arrays.asList(
                ProductResponse.builder().id(1L).name("Deleted Product 1").build(),
                ProductResponse.builder().id(2L).name("Deleted Product 2").build()
        );

        // Mocking the productService.getDeletedProducts() method
        when(productService.getDeletedProducts()).thenReturn(deletedProducts);

        // Performing the GET request to /api/products/deleted
        mockMvc.perform(get("/api/products/deleted"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Deleted Product 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Deleted Product 2"));

        verify(productService, times(1)).getDeletedProducts();
    }

//    @Test
//    @DisplayName("getAllProductsFilteredAndSorted - Should Get All Products Filtered And Sorted")
//    void shouldGetAllProductsFilteredAndSorted() {
//        // Mocked page of product responses
//        List<ProductResponse> productResponses = Arrays.asList(
//                ProductResponse.builder().id(1L).name("Product 1").build(),
//                ProductResponse.builder().id(2L).name("Product 2").build()
//        );
//        Page<ProductResponse> productResponsePage = new PageImpl<>(productResponses);
//
//        // Mocking the productService.getAllProductsFilteredAndSorted() method
//        when(productService.getAllProductsFilteredAndSorted(
//                any(Pageable.class),
//                eq(null),
//                eq(null),
//                eq(null),
//                eq(null))
//        ).thenReturn(productResponsePage);
//
//        // Performing the controller method call
//        ResponseEntity<Page<ProductResponse>> responseEntity = productController.getAllProductsFilteredAndSorted(0, 10, "id", "ASC", null, null, null, null);
//
//        // Verifying the response entity
//        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
//        assertThat(responseEntity.getBody()).isNotNull();
//        assertThat(responseEntity.getBody().getContent()).hasSize(2);
//        assertThat(responseEntity.getBody().getContent().get(0).getId()).isEqualTo(1L);
//        assertThat(responseEntity.getBody().getContent().get(0).getName()).isEqualTo("Product 1");
//        assertThat(responseEntity.getBody().getContent().get(1).getId()).isEqualTo(2L);
//        assertThat(responseEntity.getBody().getContent().get(1).getName()).isEqualTo("Product 2");
//
//        verify(productService, times(1)).getAllProductsFilteredAndSorted(
//                ArgumentMatchers.any(),  // Pageable argument
//                ArgumentMatchers.eq(null),
//                ArgumentMatchers.eq(null),
//                ArgumentMatchers.eq(null),
//                ArgumentMatchers.eq(null)
//        );
//    }


    @Test
    @DisplayName("createProduct - Should Create Product")
    public void shouldCreateProduct() throws Exception {
        // Mocked product request
        ProductRequest productRequest = ProductRequest.builder()
                .name("New Product")
                .description("Description of the new product")
                .price(BigDecimal.valueOf(100))
                .build();

        // Mocked product response
        ProductResponse productResponse = ProductResponse.builder()
                .id(1L)
                .name("New Product")
                .description("Description of the new product")
                .price(BigDecimal.valueOf(100))
                .build();

        // Mocking the productService.createProduct() method
        when(productService.createProduct(productRequest)).thenReturn(productResponse);

        // Performing the POST request to /api/products with the productRequest as the request body
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(productRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.description").value("Description of the new product"))
                .andExpect(jsonPath("$.price").value(100));

        verify(productService, times(1)).createProduct(productRequest);
    }

    @Test
    @DisplayName("updateProduct - Should Update Product")
    public void shouldUpdateProduct() throws Exception {
        long productId = 1L;

        // Mocked updated product request
        ProductRequest updatedProductRequest = ProductRequest.builder()
                .name("Updated Product")
                .description("Updated description")
                .price(BigDecimal.valueOf(200))
                .build();

        // Mocked updated product response
        ProductResponse updatedProductResponse = ProductResponse.builder()
                .id(productId)
                .name("Updated Product")
                .description("Updated description")
                .price(BigDecimal.valueOf(200))
                .build();

        // Mocking the productService.updateProduct() method
        when(productService.updateProduct(productId, updatedProductRequest)).thenReturn(updatedProductResponse);

        // Performing the PUT request to /api/products/{id} with the updatedProductRequest as the request body
        mockMvc.perform(put("/api/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatedProductRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.price").value(200));

        verify(productService, times(1)).updateProduct(productId, updatedProductRequest);
    }

    @Test
    @DisplayName("deleteProduct - Should Delete Product")
    public void shouldDeleteProduct() throws Exception {
        long productId = 1L;

        // Mocking the productService.deleteProduct() method
        doNothing().when(productService).deleteProduct(productId);

        // Performing the DELETE request to /api/products/{id}
        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(content().string("Product deleted successfully!"));

        verify(productService, times(1)).deleteProduct(productId);
    }


    // Helper method to convert objects to JSON string
    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
