package com.rapidcart.order_service.dto;

import com.rapidcart.order_service.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a order-related event that is published to RabbitMQ when an order action occurs.
 * <p>
 * This DTO is used to notify other microservices (such as inventory or analytics services)
 * about product or order-related changes, such as order creation or stock updates.
 *
 * Fields:
 * - {@code eventType}: The type of event (e.g., "ORDER_CREATED", "STOCK_UPDATED").
 * - {@code data}: The {@link Order} entity associated with this event.
 * - {@code timestamp}: The time when the event was created, set automatically to the current time.
 *
 * Example usage:
 * <pre>
 * ProductEvent event = new ProductEvent("ORDER_CREATED", order);
 * </pre>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private String eventType;
    private Order data;
    private LocalDateTime timestamp = LocalDateTime.now();

    public OrderEvent(String eventType, Order data) {
        this.eventType = eventType;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
}