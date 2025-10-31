package com.rapidcart.notification_service.service;

import com.rapidcart.notification_service.dto.ProductEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * {@code ProductEventListener} is a Spring service responsible for listening to
 * product-related events from a RabbitMQ queue and triggering appropriate
 * notification actions based on the event type.
 *
 * <p>This listener consumes messages from the {@code product.events.queue},
 * which are published by other microservices (e.g., Order Service) via a
 * {@code TopicExchange}. Upon receiving an event, it validates the event type
 * and performs notification logic accordingly.</p>
 *
 * <p>Currently, it handles the {@code ORDER_CREATED} event type and logs a
 * notification message. In a real-world scenario, this can be extended to
 * send notifications through various channels such as email, SMS, or push
 * notifications.</p>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>
 * {
 *   "eventType": "ORDER_CREATED",
 *   "data": {
 *     "id": 1,
 *     "productName": "iPhone 16 Pro",
 *     "quantity": 2,
 *     "totalPrice": 2999.98
 *   },
 *   "timestamp": "2025-10-31T12:00:00"
 * }
 * </pre>
 *
 * @author
 * @since 1.0
 */
@Slf4j
@Service
public class ProductEventListener {

    /**
     * Listens for {@link ProductEvent} messages from the {@code product.events.queue}
     * and processes them according to the event type.
     *
     * @param event the {@link ProductEvent} received from RabbitMQ
     */
    @RabbitListener(queues = "product.events.queue")
    public void handleProductEvent(ProductEvent event) {
        log.info("Received Product Event: {}", event);

        if (event.getEventType().equals("ORDER_CREATED")) {
            sendNotification(event);
        } else {
            log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    /**
     * Sends a notification for the received {@link ProductEvent}.
     * <p>
     * This method currently logs the notification details, but in a production
     * environment, it can be extended to integrate with notification channels
     * such as email, SMS, or push services.
     * </p>
     *
     * @param event the {@link ProductEvent} to send a notification for
     */
    private void sendNotification(ProductEvent event) {
        log.info("📢 Sending notification: {} -> {}", "New Order created!", event.getData());
        // In a real-world app, this could send an email, push notification, or Slack message.
    }
}
