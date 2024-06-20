package com.bit.reportservice;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "com.bit.reportservice.controller",
        "com.bit.reportservice.service"
})
class ReportServiceSuite {}
