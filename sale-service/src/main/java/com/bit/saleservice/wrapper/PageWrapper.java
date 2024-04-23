package com.bit.saleservice.wrapper;

import lombok.Data;

import java.util.List;

@Data
public class PageWrapper<SaleResponse> {
    private List<SaleResponse> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
}
