package com.bit.saleservice.controller;

import com.bit.saleservice.SaleServiceApplication;
import com.bit.saleservice.dto.SaleRequest;
import com.bit.saleservice.dto.SaleResponse;
import com.bit.saleservice.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/sales")
public class SaleController {

    private final Logger logger = LogManager.getLogger(SaleServiceApplication.class);
    private final SaleService saleService;

    @PostMapping()
    public ResponseEntity<SaleResponse> createSale(@RequestBody SaleRequest saleRequest){
        logger.info("Received request to create sale: {}", saleRequest);
         SaleResponse saleResponse = saleService.createSale(saleRequest);

        logger.info("Returning sale response's ID: {}", saleResponse.getId());
        return new ResponseEntity<>(saleResponse, HttpStatus.CREATED);
    }
}
