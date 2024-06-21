package com.bit.saleservice.service;

import com.bit.saleservice.dto.ProductResponse;
import com.bit.saleservice.exception.HeaderProcessingException;
import com.bit.saleservice.exception.ProductNotFoundException;
import com.bit.saleservice.exception.ProductReturnException;
import com.bit.saleservice.exception.ProductServiceException;
import com.bit.saleservice.wrapper.ProductStockReturnRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewayServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GatewayService gatewayService;


    @BeforeEach
    void setUp() throws HeaderProcessingException {
        ReflectionTestUtils.setField(gatewayService, "GATEWAY_URL", "http://localhost:8080/");

        gatewayService = spy(gatewayService);
        lenient().doReturn(new HttpHeaders()).when(gatewayService).getHttpHeaders();
    }

    @Test
    void testGetProduct_WhenProductExists_ReturnsProductServiceResponse() throws HeaderProcessingException {
        // Arrange
        Long productId = 1L;
        ProductResponse mockResponse = new ProductResponse();
        mockResponse.setId(productId);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductResponse.class), anyLong()))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        ProductResponse response = gatewayService.getProduct(productId);

        // Assert
        assertNotNull(response);
        assertEquals(productId, response.getId());
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductResponse.class), eq(productId));
    }

    @Test
    void testGetProduct_WhenProductDoesNotExist_ThrowsProductNotFoundException() {
        // Arrange
        Long productId = 1L;

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductResponse.class), eq(productId)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> gatewayService.getProduct(productId));
    }

    @Test
    void testReturnProducts_WhenRequestIsSuccessful_DoesNotThrowException() {
        // Arrange
        ProductStockReturnRequest request = new ProductStockReturnRequest(1L, 1);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Act & Assert
        assertDoesNotThrow(() -> gatewayService.returnProducts(request));
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testReturnProducts_WhenProductDoesNotExist_ThrowsProductNotFoundException() {
        // Arrange
        ProductStockReturnRequest request = new ProductStockReturnRequest(1L, 1);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> gatewayService.returnProducts(request));
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testInitGatewayUrl_GatewayHostAndPortAreSet_GatewayUrlIsInitializedCorrectly() {
        // Arrange
        ReflectionTestUtils.setField(gatewayService, "GATEWAY_HOST", "localhost");
        ReflectionTestUtils.setField(gatewayService, "GATEWAY_PORT", "8080");

        // Act
        gatewayService.initGatewayUrl();

        // Assert
        assertEquals("http://localhost:8080/", ReflectionTestUtils.getField(gatewayService, "GATEWAY_URL"));
    }

    @Test
    void testGetProduct_RestClientException_ThrowsProductServiceException() {
        // Arrange
        ReflectionTestUtils.setField(gatewayService, "GATEWAY_URL", "http://some-domain/");
        Long id = 1L;
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(ProductResponse.class), eq(id)))
                .thenThrow(new RestClientException("REST client error"));

        // Act and Assert
        assertThrows(ProductServiceException.class, () -> gatewayService.getProduct(id));
    }

    @Test
    void testReturnProducts_RestClientException_ThrowsProductServiceException() {
        // Arrange
        ReflectionTestUtils.setField(gatewayService, "GATEWAY_URL", "http://some-domain/");
        ProductStockReturnRequest request = new ProductStockReturnRequest(1L, 1);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RestClientException("REST client error"));

        // Act and Assert
        assertThrows(ProductReturnException.class, () -> gatewayService.returnProducts(request));
    }
}