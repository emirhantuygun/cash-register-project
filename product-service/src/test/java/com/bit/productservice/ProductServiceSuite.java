package com.bit.productservice;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "com.bit.productservice.controller",
        "com.bit.productservice.service"
})
public class ProductServiceSuite {}
