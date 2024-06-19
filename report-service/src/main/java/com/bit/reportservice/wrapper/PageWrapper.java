package com.bit.reportservice.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageWrapper<SaleResponse> {
    private List<SaleResponse> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
}
