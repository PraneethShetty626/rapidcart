package com.rapidcart.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) representing product details
 * returned in API responses.
 *
 * <p>This class encapsulates the product information that
 * the client receives after performing operations such as
 * creating, updating, or retrieving a product.</p>
 *
 * <p>Example JSON response:</p>
 * <pre>
 * {
 *   "id": 101,
 *   "name": "Wireless Headphones",
 *   "sku": "WH-1000XM5",
 *   "price": 299.99,
 *   "stock": 50,
 *   "activeStatus": true
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {

    /**
     * The unique identifier of the product.
     */
    private Long id;

    /**
     * The name of the product.
     */
    private String name;

    /**
     * The unique Stock Keeping Unit (SKU) of the product.
     */
    private String sku;

    /**
     * The price of the product.
     */
    private BigDecimal price;

    /**
     * The number of items available in stock.
     */
    private Integer stock;

    /**
     * Indicates whether the product is active or available for sale.
     */
    private Boolean activeStatus;
}
