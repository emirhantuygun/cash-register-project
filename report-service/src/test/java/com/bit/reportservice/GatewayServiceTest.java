//package com.bit.reportservice;
//
//import com.bit.reportservice.dto.SaleResponse;
//import com.bit.reportservice.exception.HeaderProcessingException;
//import com.bit.reportservice.service.GatewayService;
//import com.bit.reportservice.wrapper.PageWrapper;
//import jakarta.servlet.http.HttpServletRequest;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.core.ParameterizedTypeReference;
//import org.springframework.data.domain.*;
//import org.springframework.http.*;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.web.client.*;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//
//import java.math.BigDecimal;
//import java.util.*;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class GatewayServiceTest {
//
//    @InjectMocks
//    private GatewayService gatewayService;
//
//    @Mock
//    private RestTemplate restTemplate;
//
//    @Mock
//    private HttpServletRequest httpServletRequest;
//
//    @Mock
//    private RequestContextHolder requestContextHolder;
//
//    @BeforeEach
//    public void setUp() throws HeaderProcessingException {
//        MockitoAnnotations.openMocks(this);
//        ReflectionTestUtils.setField(gatewayService, "GET_SALE_ENDPOINT", "/sale/{id}");
//        ReflectionTestUtils.setField(gatewayService, "GET_ALL_SALES_ENDPOINT", "/sales");
//        ReflectionTestUtils.setField(gatewayService, "GET_DELETED_SALES_ENDPOINT", "/sales/deleted");
//        ReflectionTestUtils.setField(gatewayService, "GET_ALL_SALES_FILTERED_AND_SORTED_ENDPOINT", "/sales/filteredAndSorted");
//
////        gatewayService = spy(gatewayService);
////        doReturn(new HttpHeaders()).when(gatewayService).getHttpHeaders();
////        gatewayService.GATEWAY_HOST = "localhost";
////        gatewayService.GATEWAY_PORT = "8080";
////        gatewayService.initGatewayUrl();
//    }
//
////    @Test
////    public void testInitGatewayUrl_SetsGatewayUrl() {
////        gatewayService.initGatewayUrl();
////        assertThat(gatewayService.GATEWAY_URL).isEqualTo("http://localhost:8080/");
////    }
//
//    @Test
//    public void testGetSale_ReturnsSaleResponse() throws Exception {
//        Long id = 1L;
//
//        SaleResponse saleResponse = new SaleResponse();
//        ResponseEntity<SaleResponse> responseEntity = new ResponseEntity<>(saleResponse, HttpStatus.OK);
//
//        when(restTemplate.exchange(
//                anyString(),
//                eq(HttpMethod.GET),
//                ArgumentMatchers.<HttpEntity<String>>any(),
//                eq(SaleResponse.class),
//                anyLong()
//        )).thenReturn(responseEntity);
//
//        SaleResponse result = gatewayService.getSale(id);
//
//        assertThat(result).isEqualTo(saleResponse);
//        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(SaleResponse.class), eq(id));
//    }
//
//    @Test
//    public void testGetAllSales_ReturnsListOfSaleResponse() throws Exception {
//        List<SaleResponse> saleResponseList = Collections.singletonList(new SaleResponse());
//        ResponseEntity<List<SaleResponse>> responseEntity = new ResponseEntity<>(saleResponseList, HttpStatus.OK);
//
//        when(restTemplate.exchange(
//                eq("http://localhost:8080/sales"),
//                eq(HttpMethod.GET),
//                any(HttpEntity.class),
//                ArgumentMatchers.<ParameterizedTypeReference<List<SaleResponse>>>any()
//        )).thenReturn(responseEntity);
//
//        List<SaleResponse> result = gatewayService.getAllSales();
//
//        assertThat(result).isEqualTo(saleResponseList);
//        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), ArgumentMatchers.<ParameterizedTypeReference<List<SaleResponse>>>any());
//    }
//
//    @Test
//    public void testGetDeletedSales_ReturnsListOfSaleResponse() throws Exception {
//        List<SaleResponse> saleResponseList = Collections.singletonList(new SaleResponse());
//        ResponseEntity<List<SaleResponse>> responseEntity = new ResponseEntity<>(saleResponseList, HttpStatus.OK);
//
//        when(restTemplate.exchange(
//                eq("http://localhost:8080/sales/deleted"),
//                eq(HttpMethod.GET),
//                any(HttpEntity.class),
//                ArgumentMatchers.<ParameterizedTypeReference<List<SaleResponse>>>any()
//        )).thenReturn(responseEntity);
//
//        List<SaleResponse> result = gatewayService.getDeletedSales();
//
//        assertThat(result).isEqualTo(saleResponseList);
//        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), ArgumentMatchers.<ParameterizedTypeReference<List<SaleResponse>>>any());
//    }
//
////    @Test
////    public void testGetAllSalesFilteredAndSorted_ReturnsPageOfSaleResponse() throws Exception {
////        List<SaleResponse> saleResponseList = Collections.singletonList(new SaleResponse());
////        PageWrapper<SaleResponse> pageWrapper = new PageWrapper<>(saleResponseList, 1);
////        ResponseEntity<PageWrapper<SaleResponse>> responseEntity = new ResponseEntity<>(pageWrapper, HttpStatus.OK);
////
////        int page = 0;
////        int size = 10;
////        String sortBy = "id";
////        String direction = "ASC";
////        String cashier = "cashier";
////        String paymentMethod = "credit";
////        BigDecimal minPrice = BigDecimal.valueOf(10);
////        BigDecimal maxPrice = BigDecimal.valueOf(100);
////        String startDate = "2022-01-01";
////        String endDate = "2022-12-31";
////
////        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8080/sales/filteredAndSorted")
////                .queryParam("page", page)
////                .queryParam("size", size)
////                .queryParam("sortBy", sortBy)
////                .queryParam("direction", direction)
////                .queryParam("cashier", cashier)
////                .queryParam("paymentMethod", paymentMethod)
////                .queryParam("minPrice", minPrice)
////                .queryParam("maxPrice", maxPrice)
////                .queryParam("startDate", startDate)
////                .queryParam("endDate", endDate);
////
////        when(restTemplate.exchange(
////                eq(builder.toUriString()),
////                eq(HttpMethod.GET),
////                any(HttpEntity.class),
////                ArgumentMatchers.<ParameterizedTypeReference<PageWrapper<SaleResponse>>>any()
////        )).thenReturn(responseEntity);
////
////        Page<SaleResponse> result = gatewayService.getAllSalesFilteredAndSorted(page, size, sortBy, direction, cashier, paymentMethod, minPrice, maxPrice, startDate, endDate);
////
////        assertThat(result.getContent()).isEqualTo(saleResponseList);
////        assertThat(result.getTotalElements()).isEqualTo(1);
////        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), ArgumentMatchers.<ParameterizedTypeReference<PageWrapper<SaleResponse>>>any());
////    }
//
//    @Test
//    public void testGetHttpHeaders_ReturnsHttpHeaders() throws HeaderProcessingException {
//        String token = "Bearer token";
//        HttpHeaders expectedHeaders = new HttpHeaders();
//        expectedHeaders.setContentType(MediaType.APPLICATION_JSON);
//        expectedHeaders.set(HttpHeaders.AUTHORIZATION, token);
//
//        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
//        when(RequestContextHolder.getRequestAttributes()).thenReturn(attributes);
//        when(attributes.getRequest()).thenReturn(httpServletRequest);
//        when(httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(token);
//
//        HttpHeaders result = gatewayService.getHttpHeaders();
//
//        assertThat(result).isEqualTo(expectedHeaders);
//    }
//
//    @Test
//    public void testGetHttpHeaders_ThrowsHeaderProcessingException_WhenNoRequestAttributesFound() {
//        RequestContextHolder requestContextHolder1 = mock();
//        when(requestContextHolder1.getRequestAttributes()).thenReturn(null);
//
//        assertThrows(HeaderProcessingException.class, () -> gatewayService.getHttpHeaders());
//    }
//
//    @Test
//    public void testGetHttpHeaders_ThrowsHeaderProcessingException_WhenProcessingHeadersFails() {
//        when(requestContextHolder.getRequestAttributes()).thenThrow(new RuntimeException("Failed to process headers"));
//
//        assertThrows(HeaderProcessingException.class, () -> gatewayService.getHttpHeaders());
//    }
//}
