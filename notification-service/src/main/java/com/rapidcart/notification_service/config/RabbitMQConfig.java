package com.rapidcart.notification_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * RabbitMQ configuration class for the Notification Service.
 * <p>
 * This configuration sets up the necessary RabbitMQ components —
 * including the exchange, queue, binding, and message converter —
 * to handle inter-service communication between the Order Service
 * and Notification Service.
 * </p>
 *
 * <h3>Overview:</h3>
 * <ul>
 *   <li><b>Exchange:</b> {@code order.exchange} – a topic exchange for routing order-related events.</li>
 *   <li><b>Queue:</b> {@code order.events.queue} – a durable queue that stores messages until processed.</li>
 *   <li><b>Routing Key:</b> {@code order.event} – defines the message routing path for order events.</li>
 *   <li><b>Message Conversion:</b> Uses {@link Jackson2JsonMessageConverter} for seamless JSON serialization/deserialization.</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * rabbitTemplate.convertAndSend(
 *     RabbitMQConfig.EXCHANGE_NAME,
 *     RabbitMQConfig.ROUTING_KEY,
 *     new ProductEvent("ORDER_PLACED", orderDto)
 * );
 * </pre>
 *
 * This configuration ensures that order-related events published from
 * other microservices (e.g., the Order Service) are properly routed
 * and consumed by the Notification Service.
 *
 * @author
 * @version 1.0
 * @since 2025-10
 */
@Configuration
public class RabbitMQConfig {

    /**
     * The name of the topic exchange used for order-related events.
     */
    public static final String EXCHANGE_NAME = "order.exchange";

    /**
     * The name of the queue that receives order event messages.
     */
    public static final String QUEUE_NAME = "order.events.queue";

    /**
     * The routing key used to bind messages to the order events queue.
     */
    public static final String ROUTING_KEY = "order.event";

    /**
     * Declares a topic exchange for routing order-related events.
     * <p>
     * Topic exchanges allow flexible routing based on patterns in routing keys.
     * </p>
     *
     * @return a {@link TopicExchange} named {@code order.exchange}
     */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    /**
     * Declares a durable queue to hold order event messages.
     * <p>
     * The queue remains available even if RabbitMQ restarts.
     * </p>
     *
     * @return a durable {@link Queue} named {@code order.events.queue}
     */
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true);
    }

    /**
     * Binds the {@code order.events.queue} to the {@code order.exchange} using the routing key {@code order.event}.
     * <p>
     * This ensures that all messages with the routing key {@code order.event} are delivered to the queue.
     * </p>
     *
     * @param queue     the queue to bind
     * @param exchange  the exchange to bind to
     * @return a {@link Binding} connecting the queue and exchange
     */
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    /**
     * Configures a JSON message converter for serializing and deserializing messages.
     * <p>
     * This allows Java objects to be automatically converted to JSON when sent through RabbitMQ.
     * </p>
     *
     * @return a {@link MessageConverter} that uses Jackson for JSON conversion
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configures the {@link RabbitTemplate} to use the JSON message converter.
     * <p>
     * This ensures that all outgoing and incoming RabbitMQ messages are automatically converted to/from JSON.
     * </p>
     *
     * @param connectionFactory the connection factory for RabbitMQ
     * @return a configured {@link RabbitTemplate} instance
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
