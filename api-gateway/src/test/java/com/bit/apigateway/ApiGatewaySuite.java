package com.bit.apigateway;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "com.bit.apigateway.config",
        "com.bit.apigateway.controller",
        "com.bit.apigateway.util."
})
class ApiGatewaySuite {}
