package com.rapidcart.notification_service.service;


import com.rapidcart.notification_service.dto.ProductEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductEventListener {

    @RabbitListener(queues = "product.events.queue")
    public void handleProductEvent(ProductEvent event) {
        log.info("Received Product Event: {}", event);

        if (event.getEventType().equals("ORDER_CREATED")) {
            sendNotification(event);
        } else {
            log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void sendNotification(ProductEvent event) {
        log.info("📢 Sending notification: {} -> {}", "New Order created!", event.getData());
        // In a real-world app, this could send an email, push notification, or Slack message.
    }
}