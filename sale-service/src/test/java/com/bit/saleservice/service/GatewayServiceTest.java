package com.bit.saleservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import com.bit.saleservice.dto.ProductServiceResponse;
import com.bit.saleservice.exception.HeaderProcessingException;
import com.bit.saleservice.exception.ProductNotFoundException;
import com.bit.saleservice.wrapper.ProductStockCheckRequest;
import com.bit.saleservice.wrapper.ProductStockReturnRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import reactor.core.publisher.Mono;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class GatewayServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private GatewayService gatewayService;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private ResponseSpec responseSpec;

    @Value("${gateway.host}")
    private String gatewayHost;

    @Value("${gateway.port}")
    private String gatewayPort;

    @Value("${endpoint.product-service.get-product}")
    private String getProductEndpoint;

    @Value("${endpoint.product-service.check-stock}")
    private String checkStockEndpoint;

    @Value("${endpoint.product-service.return-products}")
    private String returnProductsEndpoint;

    @BeforeEach
    public void setUp() throws HeaderProcessingException {
        ReflectionTestUtils.setField(gatewayService, "GATEWAY_HOST", gatewayHost);
        ReflectionTestUtils.setField(gatewayService, "GATEWAY_PORT", gatewayPort);
        ReflectionTestUtils.setField(gatewayService, "GET_PRODUCT_ENDPOINT", getProductEndpoint);
        ReflectionTestUtils.setField(gatewayService, "CHECK_STOCK_ENDPOINT", checkStockEndpoint);
        ReflectionTestUtils.setField(gatewayService, "RETURN_PRODUCTS_ENDPOINT", returnProductsEndpoint);
        gatewayService = spy(gatewayService);
        doReturn(new HttpHeaders()).when(gatewayService).getHttpHeaders();
    }

    @Test
    public void testGetProduct_WhenProductExists_ReturnsProductServiceResponse() throws HeaderProcessingException {
        Long productId = 1L;
        ProductServiceResponse mockResponse = new ProductServiceResponse();
        mockResponse.setId(productId);
        String getUrl = "http://" + gatewayHost + ":" + gatewayPort + "/" + getProductEndpoint;

        when(restTemplate.exchange(eq(getUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductServiceResponse.class), eq(productId)))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        ProductServiceResponse response = gatewayService.getProduct(productId);

        assertNotNull(response);
        assertEquals(productId, response.getId());
        verify(restTemplate).exchange(eq(getUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductServiceResponse.class), eq(productId));
    }

    @Test
    public void testGetProduct_WhenProductDoesNotExist_ThrowsProductNotFoundException() throws HeaderProcessingException {
        Long productId = 1L;
        String getUrl = "http://" + gatewayHost + ":" + gatewayPort + "/" + getProductEndpoint;

        when(restTemplate.exchange(eq(getUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductServiceResponse.class), eq(productId)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(ProductNotFoundException.class, () -> gatewayService.getProduct(productId));
        verify(restTemplate).exchange(eq(getUrl), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductServiceResponse.class), eq(productId));
    }

    @Test
    public void testCheckEnoughProductsInStock_WhenStockIsSufficient_ReturnsTrue() throws HeaderProcessingException {
        ProductStockCheckRequest request = new ProductStockCheckRequest(1L, 1);
        Boolean mockResponse = true;
        String checkUrl = "http://" + gatewayHost + ":" + gatewayPort + "/" + checkStockEndpoint;

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(eq(checkUrl))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.headers(any())).thenReturn((WebClient.RequestBodySpec) requestHeadersSpec);
        when(requestBodyUriSpec.body(any(BodyInserter.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.just(mockResponse));

        Mono<Boolean> responseMono = gatewayService.checkEnoughProductsInStock(request);
        Boolean response = responseMono.block();

        assertNotNull(response);
        assertTrue(response);
        verify(webClient.post()).uri(eq(checkUrl));
    }

    @Test
    public void testReturnProducts_WhenRequestIsSuccessful_DoesNotThrowException() throws HeaderProcessingException {
        ProductStockReturnRequest request = new ProductStockReturnRequest(1L, 1);
        String returnUrl = "http://" + gatewayHost + ":" + gatewayPort + "/" + returnProductsEndpoint;

        when(restTemplate.exchange(eq(returnUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertDoesNotThrow(() -> gatewayService.returnProducts(request));
        verify(restTemplate).exchange(eq(returnUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void testReturnProducts_WhenProductDoesNotExist_ThrowsProductNotFoundException() throws HeaderProcessingException {
        ProductStockReturnRequest request = new ProductStockReturnRequest(1L, 1);
        String returnUrl = "http://" + gatewayHost + ":" + gatewayPort + "/" + returnProductsEndpoint;

        when(restTemplate.exchange(eq(returnUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(ProductNotFoundException.class, () -> gatewayService.returnProducts(request));
        verify(restTemplate).exchange(eq(returnUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }
}