package com.bit.usermanagementservice;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "com.bit.usermanagementservice.controller",
        "com.bit.usermanagementservice.service"
})
class UserManagementServiceSuite {}
