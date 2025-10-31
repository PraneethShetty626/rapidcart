package com.rapidcart.order_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The {@code RabbitMQConfig} class defines the RabbitMQ configuration for the
 * Order Service within the RapidCart application.
 * <p>
 * It sets up the necessary components to enable message-based communication
 * between microservices, including:
 * <ul>
 *     <li>A {@link TopicExchange} for publishing product-related events</li>
 *     <li>A durable {@link Queue} for consuming product events</li>
 *     <li>A {@link Binding} to connect the queue and exchange using a routing key</li>
 *     <li>A {@link RabbitTemplate} configured with a JSON message converter</li>
 * </ul>
 * <p>
 * The configuration ensures that messages are serialized and deserialized using JSON,
 * enabling seamless communication between services.
 *
 * <p><b>Exchange/Queue Details:</b></p>
 * <ul>
 *     <li>Exchange: {@code product.exchange}</li>
 *     <li>Queue: {@code product.events.queue}</li>
 *     <li>Routing Key: {@code product.event}</li>
 * </ul>
 *
 * Example:
 * <pre>
 * {@code
 * rabbitTemplate.convertAndSend(
 *     RabbitMQConfig.EXCHANGE_NAME,
 *     RabbitMQConfig.ROUTING_KEY,
 *     new ProductEvent("ProductCreated", product)
 * );
 * }
 * </pre>
 *
 * @author
 * @since 1.0
 */
@Configuration
public class RabbitMQConfig {

    /** The name of the topic exchange used for order events. */
    public static final String EXCHANGE_NAME = "order.exchange";

    /** The name of the durable queue used for order event messages. */
    public static final String QUEUE_NAME = "order.events.queue";

    /** The routing key used to bind the exchange and queue. */
    public static final String ROUTING_KEY = "order.event";

    /**
     * Declares a {@link TopicExchange} that allows messages to be routed based on a pattern.
     *
     * @return a configured {@link TopicExchange}
     */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    /**
     * Declares a durable {@link Queue} to store product event messages.
     *
     * @return a configured {@link Queue}
     */
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true);
    }

    /**
     * Creates a {@link Binding} between the queue and exchange using the specified routing key.
     *
     * @param queue the queue to bind
     * @param exchange the exchange to bind to
     * @return a configured {@link Binding}
     */
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    /**
     * Configures a {@link MessageConverter} that serializes messages to JSON format
     * and deserializes them back to Java objects.
     *
     * @return a {@link Jackson2JsonMessageConverter} instance
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Configures a {@link RabbitTemplate} with a JSON message converter for sending messages.
     *
     * @param connectionFactory the RabbitMQ {@link ConnectionFactory}
     * @return a configured {@link RabbitTemplate}
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
