package com.bit.productservice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark methods that should be excluded from code coverage reports.
 * It is typically used in testing scenarios where certain methods are not relevant to the test coverage.
 *
 * @author Emirhan Tuygun
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExcludeFromGeneratedCoverage {
}
