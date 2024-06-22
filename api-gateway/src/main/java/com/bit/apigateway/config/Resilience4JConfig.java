package com.bit.apigateway.config;

import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import java.time.Duration;

/**
 * This class is responsible for configuring the default settings for Resilience4J Circuit Breaker and Time Limiter.
 * It uses Spring Cloud's ReactiveResilience4JCircuitBreakerFactory and Resilience4JConfigBuilder to achieve this.
 *
 * @author Emirhan Tuygun
 */
@Configuration
public class Resilience4JConfig {

    /**
     * This method is a customizer for the default configuration of Resilience4J Circuit Breaker and Time Limiter.
     * It uses Spring Cloud's ReactiveResilience4JCircuitBreakerFactory and Resilience4JConfigBuilder to achieve this.
     *
     * @return a Customizer object that configures the default settings for the Circuit Breaker and Time Limiter.
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer(){
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .slidingWindowSize(10)
                        .waitDurationInOpenState(Duration.ofSeconds(1))
                        .build())
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(30))
                        .build())
                .build());
    }
}
