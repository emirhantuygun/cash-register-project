package com.bit.saleservice.service;

import com.bit.saleservice.dto.ProductServiceResponse;
import com.bit.saleservice.exception.HeaderProcessingException;
import com.bit.saleservice.exception.ProductNotFoundException;
import com.bit.saleservice.exception.ProductServiceException;
import com.bit.saleservice.exception.ServerErrorException;
import com.bit.saleservice.wrapper.ProductStockCheckRequest;
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
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GatewayServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private GatewayService gatewayService;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private ResponseSpec responseSpec;


    @BeforeEach
    public void setUp() throws HeaderProcessingException {
        gatewayService = spy(gatewayService);
        lenient().doReturn(new HttpHeaders()).when(gatewayService).getHttpHeaders();
    }

    @Test
    public void testGetProduct_WhenProductExists_ReturnsProductServiceResponse() throws HeaderProcessingException {
        // Arrange
        Long productId = 1L;
        ProductServiceResponse mockResponse = new ProductServiceResponse();
        mockResponse.setId(productId);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductServiceResponse.class), anyLong()))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        // Act
        ProductServiceResponse response = gatewayService.getProduct(productId);

        // Assert
        assertNotNull(response);
        assertEquals(productId, response.getId());
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductServiceResponse.class), eq(productId));
    }

    @Test
    public void testGetProduct_WhenProductDoesNotExist_ThrowsProductNotFoundException() {
        // Arrange
        Long productId = 1L;

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductServiceResponse.class), eq(productId)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // Act & Assert
        assertThrows(ProductNotFoundException.class, () -> gatewayService.getProduct(productId));
    }

    @Test
    public void testCheckEnoughProductsInStock_WhenStockIsSufficient_ReturnsTrue() throws HeaderProcessingException {
        // Arrange
        ProductStockCheckRequest request = new ProductStockCheckRequest(1L, 1);
        Boolean mockResponse = true;

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.headers(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(BodyInserter.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.just(mockResponse));

        // Act
        Mono<Boolean> responseMono = gatewayService.checkEnoughProductsInStock(request);
        Boolean response = responseMono.block();

        // Assert
        assertNotNull(response);
        assertTrue(response);
        verify(webClient.post()).uri(anyString());
    }

    @Test
    public void testReturnProducts_WhenRequestIsSuccessful_DoesNotThrowException() {
        // Arrange
        ProductStockReturnRequest request = new ProductStockReturnRequest(1L, 1);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // Act & Assert
        assertDoesNotThrow(() -> gatewayService.returnProducts(request));
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void testReturnProducts_WhenProductDoesNotExist_ThrowsProductNotFoundException() {
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
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(ProductServiceResponse.class), eq(id)))
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
        assertThrows(ProductServiceException.class, () -> gatewayService.returnProducts(request));
    }

    //*******************

    @Test
    void testCheckEnoughProductsInStock_ProductFound_ReturnsTrue() throws HeaderProcessingException {
        // Arrange
        ProductStockCheckRequest request = new ProductStockCheckRequest(1L, 1);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserter.class))).thenReturn(requestHeadersSpec);
        when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.just(true));

        // Act
        Mono<Boolean> result = gatewayService.checkEnoughProductsInStock(request);

        // Assert
        assertTrue(result.block());
    }

    @Test
    void testCheckEnoughProductsInStock_ProductNotFound_ThrowsProductNotFoundException() throws HeaderProcessingException {
        // Arrange
        ProductStockCheckRequest request = new ProductStockCheckRequest(1L, 1);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserter.class))).thenReturn(requestBodySpec);
        when(responseSpec.onStatus(any(Predicate.class), any(Function.class))).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.error(new ProductNotFoundException("Product not found with id: 1")));

        // Act and Assert
        assertThrows(ProductNotFoundException.class, () -> gatewayService.checkEnoughProductsInStock(request).block());
    }

    @Test
    void testCheckEnoughProductsInStock_ClientErrorOccurred_ThrowsClientErrorException() throws HeaderProcessingException {
        // Arrange
        ProductStockCheckRequest request = new ProductStockCheckRequest(1L, 1);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserter.class))).thenReturn(requestBodySpec);
        when(responseSpec.onStatus(any(Predicate.class), any(Function.class))).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.error(new ClientErrorException("Client error occurred while checking product stock")));

        // Act and Assert
        assertThrows(ClientErrorException.class, () -> gatewayService.checkEnoughProductsInStock(request).block());
    }

    @Test
    void testCheckEnoughProductsInStock_ServerErrorOccurred_ThrowsServerErrorException() throws HeaderProcessingException {
        // Arrange
        ProductStockCheckRequest request = new ProductStockCheckRequest(1L);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserter.class))).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(Predicate.class), any(Function.class))).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.error(new ServerErrorException("Server error occurred while checking product stock")));

        // Act and Assert
        assertThrows(ServerErrorException.class, () -> gatewayService.checkEnoughProductsInStock(request).block());
    }

    @Test
    void testCheckEnoughProductsInStock_ErrorOccurredWhileCheckingProductStock_ThrowsProductStockCheckException() throws HeaderProcessingException {
        // Arrange
        ProductStockCheckRequest request = new ProductStockCheckRequest(1L);
        when(webClient.post()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.body(any(BodyInserter.class))).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.error(new RuntimeException("Error occurred while checking product stock")));

        // Act and Assert
        assertThrows(ProductStockCheckException.class, () -> gatewayService.checkEnoughProductsInStock(request).block());
    }
}