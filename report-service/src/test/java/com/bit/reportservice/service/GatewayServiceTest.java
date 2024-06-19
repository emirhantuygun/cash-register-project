package com.bit.reportservice.service;

import com.bit.reportservice.dto.SaleResponse;
import com.bit.reportservice.exception.HeaderProcessingException;
import com.bit.reportservice.exception.SaleServiceException;
import com.bit.reportservice.wrapper.PageWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GatewayServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GatewayService gatewayService;

    @BeforeEach
    void setUp() throws HeaderProcessingException {
        ReflectionTestUtils.setField(gatewayService, "GATEWAY_URL", "http://some-domain/");
        ReflectionTestUtils.setField(gatewayService, "GET_ALL_SALES_FILTERED_AND_SORTED_ENDPOINT", "/some-endpoint");

        gatewayService = spy(gatewayService);
        doReturn(new HttpHeaders()).when(gatewayService).getHttpHeaders();
    }

    @Test
    void getSale_Success_ReturnsSaleResponse() throws HeaderProcessingException {
        SaleResponse saleResponse = new SaleResponse();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SaleResponse.class), anyLong()))
                .thenReturn(new ResponseEntity<>(saleResponse, HttpStatus.OK));

        SaleResponse result = gatewayService.getSale(1L);

        assertNotNull(result);
        assertEquals(saleResponse, result);
    }

    @Test
    void getSale_HttpClientErrorException_ThrowsSaleServiceException() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SaleResponse.class), anyLong()))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(SaleServiceException.class, () -> gatewayService.getSale(1L));
    }

    @Test
    void getAllSales_Success_ReturnsListOfSaleResponse() throws HeaderProcessingException {
        List<SaleResponse> saleResponses = Arrays.asList(new SaleResponse(), new SaleResponse());
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(saleResponses, HttpStatus.OK));

        List<SaleResponse> result = gatewayService.getAllSales();

        assertNotNull(result);
        assertEquals(saleResponses.size(), result.size());
    }

    @Test
    void getDeletedSales_Success_ReturnsListOfSaleResponse() throws HeaderProcessingException {
        List<SaleResponse> deletedSaleResponses = Collections.singletonList(new SaleResponse());
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(deletedSaleResponses, HttpStatus.OK));

        List<SaleResponse> result = gatewayService.getDeletedSales();

        assertNotNull(result);
        assertEquals(deletedSaleResponses.size(), result.size());
    }

    @Test
    void getAllSalesFilteredAndSorted_RestClientException_ThrowsSaleServiceException() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class),
                any(ParameterizedTypeReference.class)))
                .thenThrow(new RestClientException("REST client error"));

        assertThrows(SaleServiceException.class, () ->
                gatewayService.getAllSalesFilteredAndSorted(0, 10, "id", "ASC", "cashier", "credit",
                        BigDecimal.TEN, BigDecimal.valueOf(100), "2022-01-01", "2022-12-31"));
    }

    @Test
    void getAllSalesFilteredAndSorted_Success_ReturnsPageOfSaleResponse() throws HeaderProcessingException {
        List<SaleResponse> saleResponses = Arrays.asList(new SaleResponse(), new SaleResponse());
        PageWrapper<SaleResponse> pageWrapper = new PageWrapper<>(saleResponses, 2, 10, 10L);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(new ParameterizedTypeReference<PageWrapper<SaleResponse>>() {})))
                .thenReturn(new ResponseEntity<>(pageWrapper, HttpStatus.OK));

        Page<SaleResponse> result = gatewayService.getAllSalesFilteredAndSorted(0, 10, "id", "ASC", "cashier", "credit",
                BigDecimal.TEN, BigDecimal.valueOf(100), "2022-01-01", "2022-12-31");

        assertNotNull(result);
        assertEquals(saleResponses.size(), result.getContent().size());
    }

}
