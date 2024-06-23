package com.bit.productservice.config;

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
 * This class is responsible for configuring RabbitMQ.
 * It creates an exchange, a queue, and a binding between them.
 * It also sets up a RabbitTemplate with a Jackson2JsonMessageConverter for message conversion.
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
     * This method creates a new instance of DirectExchange with the given exchange name.
     * DirectExchange delivers messages to queues based on the routing key.
     *
     * @return a new instance of DirectExchange
     */
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    /**
     * This method creates a new instance of Queue with the given queue name.
     * Queues are used to store messages that are consumed by applications.
     *
     * @return a new instance of Queue
     */
    @Bean
    public Queue queue() {
        return new Queue(QUEUE, false);
    }

    /**
     * This method creates a new binding between the queue and the exchange.
     * The binding is defined by the routing key, which determines which messages will be routed to the queue.
     *
     * @return a new instance of Binding
     */
    @Bean
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(exchange()).with(ROUTING_KEY);
    }

    /**
     * This method creates a new instance of Jackson2JsonMessageConverter.
     * Jackson2JsonMessageConverter is a message converter provided by Spring AMQP that converts messages to and from JSON.
     * It is used to serialize and deserialize messages sent and received over RabbitMQ.
     *
     * @return a new instance of Jackson2JsonMessageConverter
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * This method creates a new instance of RabbitTemplate.
     * RabbitTemplate is a core component of Spring AMQP that provides a convenient API for sending and receiving messages.
     * It uses a ConnectionFactory to establish connections to RabbitMQ and a MessageConverter to serialize and deserialize messages.
     *
     * @param connectionFactory the ConnectionFactory to establish connections to RabbitMQ
     * @param jsonMessageConverter the Jackson2JsonMessageConverter to serialize and deserialize messages
     * @return a new instance of RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
