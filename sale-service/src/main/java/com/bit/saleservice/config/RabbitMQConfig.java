package com.bit.saleservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ's configuration class.
 * This class is responsible for setting up RabbitMQ exchange, queue, binding, and message converter.
 * It also provides a RabbitTemplate for sending and receiving messages.
 *
 * @author Emirhan Tuygun
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String EXCHANGE;

    @Value("${rabbitmq.queue}")
    private String QUEUE;

    @Value("${rabbitmq.routingKey}")
    private String ROUTING_KEY;

    /**
     * Creates a DirectExchange instance with the given exchange name.
     *
     * @return the DirectExchange instance
     */
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    /**
     * Creates a Queue instance with the given queue name and durability.
     *
     * @return the Queue instance
     */
    @Bean
    public Queue queue() {
        return new Queue(QUEUE, false);
    }

    /**
     * Creates a Binding instance that binds the queue to the exchange with the given routing key.
     *
     * @return the Binding instance
     */
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(exchange()).with(ROUTING_KEY);
    }

    /**
     * Creates a Jackson2JsonMessageConverter instance for converting messages to and from JSON.
     *
     * @return the Jackson2JsonMessageConverter instance
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Creates a RabbitTemplate instance for sending and receiving messages.
     *
     * @param connectionFactory the RabbitMQ connection factory
     * @param jsonMessageConverter the Jackson2JsonMessageConverter instance
     * @return the RabbitTemplate instance
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
