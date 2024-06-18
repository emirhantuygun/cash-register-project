package com.bit.saleservice.service;

import com.bit.saleservice.dto.ProductServiceResponse;
import com.bit.saleservice.exception.HeaderProcessingException;
import com.bit.saleservice.exception.ProductNotFoundException;
import com.bit.saleservice.wrapper.ProductStockCheckRequest;
import com.bit.saleservice.wrapper.ProductStockReturnRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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


    @BeforeEach
    public void setUp() throws HeaderProcessingException {
        gatewayService = spy(gatewayService);
        doReturn(new HttpHeaders()).when(gatewayService).getHttpHeaders();
    }

    @Test
    public void testGetProduct_WhenProductExists_ReturnsProductServiceResponse() throws HeaderProcessingException {
        Long productId = 1L;
        ProductServiceResponse mockResponse = new ProductServiceResponse();
        mockResponse.setId(productId);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductServiceResponse.class), anyLong()))
                .thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        ProductServiceResponse response = gatewayService.getProduct(productId);

        assertNotNull(response);
        assertEquals(productId, response.getId());
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductServiceResponse.class), eq(productId));
    }

    @Test
    public void testGetProduct_WhenProductDoesNotExist_ThrowsProductNotFoundException() {
        Long productId = 1L;

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(ProductServiceResponse.class), eq(productId)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(ProductNotFoundException.class, () -> gatewayService.getProduct(productId));
    }

    @Test
    public void testCheckEnoughProductsInStock_WhenStockIsSufficient_ReturnsTrue() throws HeaderProcessingException {
        ProductStockCheckRequest request = new ProductStockCheckRequest(1L, 1);
        Boolean mockResponse = true;

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.headers(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(BodyInserter.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.just(mockResponse));

        Mono<Boolean> responseMono = gatewayService.checkEnoughProductsInStock(request);
        Boolean response = responseMono.block();

        assertNotNull(response);
        assertTrue(response);
        verify(webClient.post()).uri(anyString());
    }

    @Test
    public void testReturnProducts_WhenRequestIsSuccessful_DoesNotThrowException() {
        ProductStockReturnRequest request = new ProductStockReturnRequest(1L, 1);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        assertDoesNotThrow(() -> gatewayService.returnProducts(request));
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void testReturnProducts_WhenProductDoesNotExist_ThrowsProductNotFoundException() {
        ProductStockReturnRequest request = new ProductStockReturnRequest(1L, 1);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(ProductNotFoundException.class, () -> gatewayService.returnProducts(request));
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }
}