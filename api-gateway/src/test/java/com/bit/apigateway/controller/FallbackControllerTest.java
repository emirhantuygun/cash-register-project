package com.bit.apigateway.controller;

import com.bit.apigateway.exception.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@WebMvcTest(FallbackController.class)
public class FallbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void whenFallbackAuthEndpointIsCalled_thenAuthServiceUnavailableExceptionIsThrown() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/fallback/auth"))
                .andExpect(MockMvcResultMatchers.status().isServiceUnavailable())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof AuthServiceUnavailableException));
    }

    @Test
    public void whenFallbackUserEndpointIsCalled_thenUserServiceUnavailableExceptionIsThrown() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/fallback/user"))
                .andExpect(MockMvcResultMatchers.status().isServiceUnavailable())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof UserServiceUnavailableException));
    }

    @Test
    public void whenFallbackProductEndpointIsCalled_thenProductServiceUnavailableExceptionIsThrown() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/fallback/product"))
                .andExpect(MockMvcResultMatchers.status().isServiceUnavailable())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ProductServiceUnavailableException));
    }

    @Test
    public void whenFallbackSaleEndpointIsCalled_thenSaleServiceUnavailableExceptionIsThrown() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/fallback/sale"))
                .andExpect(MockMvcResultMatchers.status().isServiceUnavailable())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof SaleServiceUnavailableException));
    }

    @Test
    public void whenFallbackReportEndpointIsCalled_thenReportServiceUnavailableExceptionIsThrown() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/fallback/report"))
                .andExpect(MockMvcResultMatchers.status().isServiceUnavailable())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ReportServiceUnavailableException));
    }
}