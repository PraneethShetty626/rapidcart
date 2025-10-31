package com.rapidcart.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) representing product details retrieved from the Product Service.
 * <p>
 * This class is used for communication between services to exchange product-related data,
 * particularly when creating or validating an order.
 *
 * Fields:
 * - {@code id}: The unique identifier of the product.
 * - {@code name}: The display name of the product.
 * - {@code sku}: The Stock Keeping Unit — a unique product code used for inventory tracking.
 * - {@code price}: The current selling price of the product.
 * - {@code stock}: The available quantity of the product in inventory.
 * - {@code activeStatus}: Indicates whether the product is currently active or discontinued.
 *
 * Example JSON representation:
 * <pre>
 * {
 *   "id": 5001,
 *   "name": "Wireless Mouse",
 *   "sku": "WM-2025",
 *   "price": 599.99,
 *   "stock": 25,
 *   "activeStatus": true
 * }
 * </pre>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private Integer stock;
    private Boolean activeStatus;
}
