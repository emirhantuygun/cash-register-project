package com.bit.usermanagementservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    public static final String USER_EXCHANGE = "user-exchange";
    public static final String USER_QUEUE_CREATE = "user-queue-create";
    public static final String USER_QUEUE_UPDATE = "user-queue-update";
    public static final String USER_QUEUE_DELETE = "user-queue-delete";
    public static final String USER_QUEUE_DELETE_PERMANENT = "user-queue-delete-permanent";
    public static final String USER_QUEUE_RESTORE = "user-queue-restore";
    public static final String ROUTING_KEY_CREATE = "user.create";
    public static final String ROUTING_KEY_UPDATE = "user.update";
    public static final String ROUTING_KEY_DELETE = "user.delete";
    public static final String ROUTING_KEY_DELETE_PERMANENT = "user.delete.permanent";
    public static final String ROUTING_KEY_RESTORE = "user.restore";

    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(USER_EXCHANGE);
    }

    @Bean
    public Queue userQueueCreate() {
        return new Queue(USER_QUEUE_CREATE, false);
    }

    @Bean
    public Queue userQueueUpdate() {
        return new Queue(USER_QUEUE_UPDATE, false);
    }

    @Bean
    public Queue userQueueDelete() {
        return new Queue(USER_QUEUE_DELETE, false);
    }

    @Bean
    public Queue userQueueDeletePermanent() {
        return new Queue(USER_QUEUE_DELETE_PERMANENT, false);
    }

    @Bean
    public Queue userQueueRestore() {
        return new Queue(USER_QUEUE_RESTORE, false);
    }

    @Bean
    public Binding bindingCreate() {
        return BindingBuilder.bind(userQueueCreate()).to(userExchange()).with(ROUTING_KEY_CREATE);
    }

    @Bean
    public Binding bindingUpdate() {
        return BindingBuilder.bind(userQueueUpdate()).to(userExchange()).with(ROUTING_KEY_UPDATE);
    }

    @Bean
    public Binding bindingDelete() {
        return BindingBuilder.bind(userQueueDelete()).to(userExchange()).with(ROUTING_KEY_DELETE);
    }

    @Bean
    public Binding bindingDeletePermanent() {
        return BindingBuilder.bind(userQueueDeletePermanent()).to(userExchange()).with(ROUTING_KEY_DELETE_PERMANENT);
    }

    @Bean
    public Binding bindingRestore() {
        return BindingBuilder.bind(userQueueRestore()).to(userExchange()).with(ROUTING_KEY_RESTORE);
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
