package com.bit.reportservice.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark methods that should be excluded from code coverage reports.
 * It is intended to be used in unit tests to ensure that certain methods are not counted towards
 * the overall code coverage.
 *
 * @author Emirhan Tuygun
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExcludeFromGeneratedCoverage {
}
