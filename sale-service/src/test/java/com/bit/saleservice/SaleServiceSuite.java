package com.bit.saleservice;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "com.bit.saleservice.controller",
        "com.bit.saleservice.service"
})
public class SaleServiceSuite {
}
