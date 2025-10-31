package com.rapidcart.order_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object (DTO) representing the payload required to create a new order.
 * <p>
 * This class is used for incoming requests from clients when placing an order.
 * It includes validation annotations to ensure that all required fields are provided
 * and contain valid values before processing.
 *
 * Fields:
 * - {@code customerId}: The unique ID of the customer placing the order.
 * - {@code productId}: The unique ID of the product being ordered.
 * - {@code quantity}: The number of product units requested (must be positive).
 *
 * Validation:
 * - {@link NotNull} ensures none of the fields are null.
 * - {@link Positive} ensures the quantity is greater than zero.
 *
 * Example JSON request:
 * <pre>
 * {
 *   "customerId": 101,
 *   "productId": 5001,
 *   "quantity": 3
 * }
 * </pre>
 */
@Data
@Builder
@AllArgsConstructor
public class OrderRequestDto {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}
