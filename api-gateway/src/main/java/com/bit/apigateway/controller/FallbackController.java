package com.bit.apigateway.controller;

import com.bit.apigateway.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class FallbackController {

    @RequestMapping(value = "/fallback/auth", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackAuth() {
        throw new AuthServiceUnavailableException("Auth Service is temporarily unavailable. Please try again later.");
    }

    @RequestMapping(value = "/fallback/user", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackUser() {
        throw new UserServiceUnavailableException("User Service is temporarily unavailable. Please try again later.");
    }

    @RequestMapping(value = "/fallback/product", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackProduct() {
        throw new ProductServiceUnavailableException("Product Service is temporarily unavailable. Please try again later.");
    }

    @RequestMapping(value = "/fallback/sale", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackSale() {
        throw new SaleServiceUnavailableException("Sale Service is temporarily unavailable. Please try again later.");
    }

    @RequestMapping(value = "/fallback/report", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackReport() {
        throw new ReportServiceUnavailableException("Report Service is temporarily unavailable. Please try again later.");
    }
}
