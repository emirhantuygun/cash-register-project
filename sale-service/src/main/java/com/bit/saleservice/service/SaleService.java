package com.bit.saleservice.service;

import com.bit.saleservice.dto.SaleRequest;
import com.bit.saleservice.dto.SaleResponse;

public interface SaleService {

    SaleResponse createSale(SaleRequest saleRequest);
}
