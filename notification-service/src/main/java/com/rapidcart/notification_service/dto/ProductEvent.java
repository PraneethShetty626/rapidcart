package com.rapidcart.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a product-related event in the notification service.
 * <p>
 * This class is typically used to encapsulate event data published to or received from
 * a message broker (e.g., RabbitMQ or Kafka) to notify other services of changes related
 * to products or orders.
 * </p>
 *
 * <h3>Structure:</h3>
 * <ul>
 *   <li>{@code eventType} – the type of the event (e.g., {@code "PRODUCT_CREATED"}, {@code "ORDER_PLACED"})</li>
 *   <li>{@code data} – the associated product or order details as a {@link ProductOrderDto}</li>
 *   <li>{@code timestamp} – the time when the event was created or published</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * ProductOrderDto orderDto = new ProductOrderDto(1L, "Laptop", 2, new BigDecimal("2500.00"));
 * ProductEvent event = new ProductEvent("ORDER_PLACED", orderDto);
 * </pre>
 *
 * This object can then be serialized and sent to a message queue for downstream processing.
 *
 * @author
 * @version 1.0
 * @since 2025-10
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEvent {

    /**
     * The type of the event (e.g., {@code "PRODUCT_CREATED"}, {@code "ORDER_PLACED"}, {@code "STOCK_UPDATED"}).
     */
    private String eventType;

    /**
     * The payload containing product or order details.
     */
    private ProductOrderDto data;

    /**
     * The timestamp when the event was generated.
     * <p>
     * Automatically initialized to the current time upon object creation.
     * </p>
     */
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Constructs a {@code ProductEvent} with the specified event type and data.
     * <p>
     * The {@code timestamp} field is automatically set to the current time.
     * </p>
     *
     * @param eventType the type of event (e.g., {@code "ORDER_PLACED"})
     * @param data      the event payload containing product/order details
     */
    public ProductEvent(String eventType, ProductOrderDto data) {
        this.eventType = eventType;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
}
