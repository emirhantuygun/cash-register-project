package com.bit.usermanagementservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * This class is responsible for configuring a RestTemplate bean in the Spring application context.
 * The RestTemplate is a convenient utility for making HTTP requests.
 *
 * @author Emirhan Tuygun
 */
@Configuration
public class RestTemplateConfig {

    /**
     * This method creates and returns a new instance of RestTemplate.
     * RestTemplate is a core Spring class for making HTTP requests.
     *
     * @return a new instance of RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
