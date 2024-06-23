package com.bit.reportservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for RestTemplate.
 * This class provides a RestTemplate bean with custom message converters.
 *
 * @author Emirhan Tuygun
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates a RestTemplate bean with custom message converters.
     *
     * @return the configured RestTemplate bean
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(getMessageConverters());
        return restTemplate;
    }

    /**
     * Returns a list of HttpMessageConverter objects for customizing RestTemplate.
     * In this case, it adds MappingJackson2HttpMessageConverter for JSON support.
     *
     * @return a list of HttpMessageConverter objects
     */
    private List<HttpMessageConverter<?>> getMessageConverters() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new MappingJackson2HttpMessageConverter());
        return converters;
    }
}
