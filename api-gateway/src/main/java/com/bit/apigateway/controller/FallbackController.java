package com.bit.apigateway.controller;

import com.bit.apigateway.exception.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * This class is responsible for handling fallback requests when the corresponding service is unavailable.
 * It logs warnings and throws custom exceptions for each service.
 *
 * @author Emirhan Tuygun
 */
@Log4j2
@RestController
public class FallbackController {

    /**
     * This method is a fallback handler for the Auth service. It logs a warning message and throws a custom exception
     * when the Auth service is temporarily unavailable.
     *
     * @return Returns a ResponseEntity with a warning message and HTTP status code 503 (Service Unavailable)
     * @throws AuthServiceUnavailableException Throws a custom exception when the Auth service is unavailable
     */
    @RequestMapping(value = "/fallback/auth", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackAuth() {
        log.trace("Entering fallbackAuth method in FallbackController");
        try {
            log.warn("Auth Service is temporarily unavailable. Please try again later.");
            throw new AuthServiceUnavailableException("Auth Service is temporarily unavailable. Please try again later.");
        } finally {
            log.trace("Exiting fallbackAuth method in FallbackController");
        }
    }

    /**
     * This method is a fallback handler for the User service. It logs a warning message and throws a custom exception
     * when the User service is temporarily unavailable.
     *
     * @return Returns a ResponseEntity with a warning message and HTTP status code 503 (Service Unavailable)
     * @throws UserServiceUnavailableException Throws a custom exception when the User service is unavailable
     */
    @RequestMapping(value = "/fallback/user", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackUser() {
        log.trace("Entering fallbackUser method in FallbackController");
        try {
            log.warn("User Service is temporarily unavailable. Please try again later.");
            throw new UserServiceUnavailableException("User Service is temporarily unavailable. Please try again later.");
        } finally {
            log.trace("Exiting fallbackUser method in FallbackController");
        }
    }

    /**
     * This method is a fallback handler for the Product service. It logs a warning message and throws a custom exception
     * when the Product service is temporarily unavailable.
     *
     * @return Returns a ResponseEntity with a warning message and HTTP status code 503 (Service Unavailable)
     * @throws ProductServiceUnavailableException Throws a custom exception when the Product service is unavailable
     */
    @RequestMapping(value = "/fallback/product", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackProduct() {
        log.trace("Entering fallbackProduct method in FallbackController");
        try {
            log.warn("Product Service is temporarily unavailable. Please try again later.");
            throw new ProductServiceUnavailableException("Product Service is temporarily unavailable. Please try again later.");
        } finally {
            log.trace("Exiting fallbackProduct method in FallbackController");
        }
    }

    /**
     * This method is a fallback handler for the Sale service. It logs a warning message and throws a custom exception
     * when the Sale service is temporarily unavailable.
     *
     * @return Returns a ResponseEntity with a warning message and HTTP status code 503 (Service Unavailable)
     * @throws SaleServiceUnavailableException Throws a custom exception when the Sale service is unavailable
     */
    @RequestMapping(value = "/fallback/sale", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackSale() {
        log.trace("Entering fallbackSale method in FallbackController");
        try {
            log.warn("Sale Service is temporarily unavailable. Please try again later.");
            throw new SaleServiceUnavailableException("Sale Service is temporarily unavailable. Please try again later.");
        } finally {
            log.trace("Exiting fallbackSale method in FallbackController");
        }
    }

    /**
     * This method is a fallback handler for the Report service. It logs a warning message and throws a custom exception
     * when the Report service is temporarily unavailable.
     *
     * @return Returns a ResponseEntity with a warning message and HTTP status code 503 (Service Unavailable)
     * @throws ReportServiceUnavailableException Throws a custom exception when the Report service is unavailable
     */
    @RequestMapping(value = "/fallback/report", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> fallbackReport() {
        log.trace("Entering fallbackReport method in FallbackController");
        try {
            log.warn("Report Service is temporarily unavailable. Please try again later.");
            throw new ReportServiceUnavailableException("Report Service is temporarily unavailable. Please try again later.");
        } finally {
            log.trace("Exiting fallbackReport method in FallbackController");
        }
    }
}