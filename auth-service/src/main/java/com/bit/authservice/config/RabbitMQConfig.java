package com.bit.authservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "user-creation-queue";
    public static final String EXCHANGE_NAME = "user-creation-exchange";
    public static final String ROUTING_KEY = "user-creation-routing-key";

    @Bean
    public Queue userCreationQueue() {
        return new Queue(QUEUE_NAME, false);
    }

    @Bean
    public DirectExchange userCreationExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding userCreationBinding(Queue userCreationQueue, DirectExchange userCreationExchange) {
        return BindingBuilder.bind(userCreationQueue).to(userCreationExchange).with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
