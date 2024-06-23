package com.bit.productservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * This class is responsible for configuring RedisTemplate for the application.
 * RedisTemplate is a high-level abstraction over the Redis connection and provides
 * convenient methods for interacting with Redis data structures.
 *
 * @author Emirhan Tuygun
 */
@Configuration
public class RedisConfig {

    /**
     * This method creates and configures a RedisTemplate instance.
     *
     * @param connectionFactory The connection factory for Redis.
     * @return A configured RedisTemplate instance.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());

        return template;
    }
}