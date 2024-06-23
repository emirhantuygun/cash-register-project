package com.bit.saleservice.wrapper;

import lombok.Data;

import java.util.List;

/**
 * This class represents a wrapper for paginated data.
 * It is commonly used in APIs to return a subset of data along with pagination information.
 *
 * @param <SaleResponse> The type of the data being wrapped.
 * @author Emirhan Tuygun
 */
@Data
public class PageWrapper<SaleResponse> {
    private List<SaleResponse> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
}
