package com.rapidcart.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
/**
 * Data Transfer Object (DTO) representing the response returned to clients after
 * an order is created or retrieved.
 * <p>
 * This class encapsulates all relevant order details such as product information,
 * pricing, quantity, customer details, and order creation time.
 *
 * Fields:
 * - {@code id}: The unique identifier of the order.
 * - {@code productId}: The ID of the product associated with the order.
 * - {@code productName}: The name of the product.
 * - {@code unitPrice}: The price per unit of the product at the time of ordering.
 * - {@code quantity}: The number of product units ordered.
 * - {@code totalPrice}: The total price (unitPrice × quantity) for the order.
 * - {@code customerId}: The unique identifier of the customer who placed the order.
 * - {@code createdAt}: The timestamp when the order was created.
 *
 * Example JSON response:
 * <pre>
 * {
 *   "id": 1001,
 *   "productId": 5001,
 *   "productName": "Wireless Mouse",
 *   "unitPrice": 599.99,
 *   "quantity": 2,
 *   "totalPrice": 1199.98,
 *   "customerId": 101,
 *   "createdAt": "2025-10-31T10:45:00"
 * }
 * </pre>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private Long customerId;
    private LocalDateTime createdAt;

}
