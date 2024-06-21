package com.bit.apigateway.controller;

import com.bit.apigateway.exception.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
public class FallbackController {

    @RequestMapping(value = "/fallback/auth", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackAuth() {
        log.trace("Entering fallbackAuth method in FallbackController");
        try {
            throw new AuthServiceUnavailableException("Auth Service is temporarily unavailable. Please try again later.");
        } finally {
            log.trace("Exiting fallbackAuth method in FallbackController");
        }
    }

    @RequestMapping(value = "/fallback/user", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackUser() {
        log.trace("Entering fallbackUser method in FallbackController");
        try {
            throw new UserServiceUnavailableException("User Service is temporarily unavailable. Please try again later.");
        } finally {
            log.trace("Exiting fallbackUser method in FallbackController");
        }
    }

    @RequestMapping(value = "/fallback/product", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackProduct() {
        log.trace("Entering fallbackProduct method in FallbackController");
        try {
            throw new ProductServiceUnavailableException("Product Service is temporarily unavailable. Please try again later.");
        } finally {
            log.trace("Exiting fallbackProduct method in FallbackController");
        }
    }

    @RequestMapping(value = "/fallback/sale", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackSale() {
        log.trace("Entering fallbackSale method in FallbackController");
        try {
            throw new SaleServiceUnavailableException("Sale Service is temporarily unavailable. Please try again later.");
        } finally {
            log.trace("Exiting fallbackSale method in FallbackController");
        }
    }

    @RequestMapping(value = "/fallback/report", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackReport() {
        log.trace("Entering fallbackReport method in FallbackController");
        try {
            throw new ReportServiceUnavailableException("Report Service is temporarily unavailable. Please try again later.");
        } finally {
            log.trace("Exiting fallbackReport method in FallbackController");
        }
    }
}