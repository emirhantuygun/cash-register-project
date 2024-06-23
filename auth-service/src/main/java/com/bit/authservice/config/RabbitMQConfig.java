package com.bit.authservice.config;

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
 * It includes the creation of exchanges, queues, bindings, and a RabbitTemplate.
 *
 * @see org.springframework.amqp.core.Queue
 * @see org.springframework.amqp.core.DirectExchange
 * @see org.springframework.amqp.core.Binding
 * @see org.springframework.amqp.core.BindingBuilder
 *
 * @author Emirhan Tuygun
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String EXCHANGE;

    @Value("${rabbitmq.queue.create}")
    private String USER_QUEUE_CREATE;

    @Value("${rabbitmq.queue.update}")
    private String USER_QUEUE_UPDATE;

    @Value("${rabbitmq.queue.delete}")
    private String USER_QUEUE_DELETE;

    @Value("${rabbitmq.queue.deletePermanent}")
    private String USER_QUEUE_DELETE_PERMANENT;

    @Value("${rabbitmq.queue.restore}")
    private String USER_QUEUE_RESTORE;

    @Value("${rabbitmq.routingKey.create}")
    private String ROUTING_KEY_CREATE;

    @Value("${rabbitmq.routingKey.update}")
    private String ROUTING_KEY_UPDATE;

    @Value("${rabbitmq.routingKey.delete}")
    private String ROUTING_KEY_DELETE;

    @Value("${rabbitmq.routingKey.deletePermanent}")
    private String ROUTING_KEY_DELETE_PERMANENT;

    @Value("${rabbitmq.routingKey.restore}")
    private String ROUTING_KEY_RESTORE;

    /**
     * This method creates a new instance of DirectExchange.
     * DirectExchange delivers messages to queues based on the routing key.
     *
     * @return a new instance of DirectExchange
     */
    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(EXCHANGE);
    }

    /**
     * This method creates a new instance of Queue for user creation events.
     * The queue is bound to the userExchange with the routing key for user creation events.
     *
     * @return a new instance of Queue for user creation events
     */
    @Bean
    public Queue userQueueCreate() {
        return new Queue(USER_QUEUE_CREATE, false);
    }

    /**
     * This method creates a new instance of Queue for user update events.
     * The queue is bound to the userExchange with the routing key for user update events.
     *
     * @return a new instance of Queue for user update events
     */
    @Bean
    public Queue userQueueUpdate() {
        return new Queue(USER_QUEUE_UPDATE, false);
    }

    /**
     * This method creates a new instance of Queue for user delete events.
     * The queue is bound to the userExchange with the routing key for user delete events.
     *
     * @return a new instance of Queue for user delete events
     */
    @Bean
    public Queue userQueueDelete() {
        return new Queue(USER_QUEUE_DELETE, false);
    }

    /**
     * This method creates a new instance of Queue for user delete permanent events.
     * The queue is bound to the userExchange with the routing key for user delete permanent events.
     *
     * @return a new instance of Queue for user delete permanent events
     */
    @Bean
    public Queue userQueueDeletePermanent() {
        return new Queue(USER_QUEUE_DELETE_PERMANENT, false);
    }

    /**
     * This method creates a new instance of Queue for user restore events.
     * The queue is bound to the userExchange with the routing key for user restore events.
     *
     * @return a new instance of Queue for user restore events
     */
    @Bean
    public Queue userQueueRestore() {
        return new Queue(USER_QUEUE_RESTORE, false);
    }

    /**
     * This method creates a new instance of Binding for user creation events.
     * A Binding is a relationship between an exchange and a queue,
     * and it specifies the routing key that determines which messages will be routed to the queue.
     *
     * @return a new instance of Binding for user creation events
     */
    @Bean
    public Binding bindingCreate() {
        return BindingBuilder.bind(userQueueCreate()).to(userExchange()).with(ROUTING_KEY_CREATE);
    }

    /**
     * This method creates a new instance of Binding for user update events.
     * A Binding is a relationship between an exchange and a queue,
     * and it specifies the routing key that determines which messages will be routed to the queue.
     *
     * @return a new instance of Binding for user update events
     */
    @Bean
    public Binding bindingUpdate() {
        return BindingBuilder.bind(userQueueUpdate()).to(userExchange()).with(ROUTING_KEY_UPDATE);
    }

    /**
     * This method creates a new instance of Binding for user delete events.
     * A Binding is a relationship between an exchange and a queue,
     * and it specifies the routing key that determines which messages will be routed to the queue.
     *
     * @return a new instance of Binding for user delete events
     */
    @Bean
    public Binding bindingDelete() {
        return BindingBuilder.bind(userQueueDelete()).to(userExchange()).with(ROUTING_KEY_DELETE);
    }

    /**
     * This method creates a new instance of Binding for user delete permanent events.
     * A Binding is a relationship between an exchange and a queue,
     * and it specifies the routing key that determines which messages will be routed to the queue.
     *
     * @return a new instance of Binding for user delete permanent events
     */
    @Bean
    public Binding bindingDeletePermanent() {
        return BindingBuilder.bind(userQueueDeletePermanent()).to(userExchange()).with(ROUTING_KEY_DELETE_PERMANENT);
    }

    /**
     * This method creates a new instance of Binding for user restore events.
     * A Binding is a relationship between an exchange and a queue,
     * and it specifies the routing key that determines which messages will be routed to the queue.
     *
     * @return a new instance of Binding for user restore events
     */
    @Bean
    public Binding bindingRestore() {
        return BindingBuilder.bind(userQueueRestore()).to(userExchange()).with(ROUTING_KEY_RESTORE);
    }

    /**
     * This method creates a new instance of Jackson2JsonMessageConverter.
     * Jackson2JsonMessageConverter is a Spring AMQP message converter that uses the Jackson library to convert messages to and from JSON.
     *
     * @return a new instance of Jackson2JsonMessageConverter
     */
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * This method creates a new instance of RabbitTemplate.
     * RabbitTemplate is a high-level abstraction for sending and receiving messages.
     * It uses a ConnectionFactory to establish connections to the RabbitMQ server.
     *
     * @param connectionFactory the ConnectionFactory to establish connections to the RabbitMQ server
     * @param jsonMessageConverter the Jackson2JsonMessageConverter to convert messages to and from JSON
     * @return a new instance of RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
