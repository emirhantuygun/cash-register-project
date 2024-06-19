package com.bit.productservice;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "com.bit.authservice.config",
        "com.bit.authservice.controller",
        "com.bit.authservice.service",
        "com.bit.authservice.util."
})
public class ProductServiceSuite {
}
