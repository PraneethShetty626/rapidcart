package com.rapidcart.order_service.service;

import com.rapidcart.order_service.config.RabbitMQConfig;
import com.rapidcart.order_service.dto.OrderEvent;
import com.rapidcart.order_service.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishProductEvent(String eventType, Order payload) {
        OrderEvent event = new OrderEvent(eventType, payload);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                event
        );
    }
}