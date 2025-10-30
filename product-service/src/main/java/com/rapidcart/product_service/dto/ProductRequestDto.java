package com.rapidcart.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object (DTO) for creating or updating a product.
 *
 * <p>This class defines the structure of the product payload expected
 * in API requests. It includes validation annotations to ensure
 * the integrity of input data.</p>
 *
 * <p>Example JSON payload:</p>
 * <pre>
 * {
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
public class ProductRequestDto {

    /**
     * The name of the product.
     * <p>Cannot be blank.</p>
     */
    @NotBlank(message = "Product name is required")
    private String name;

    /**
     * The unique Stock Keeping Unit (SKU) identifier for the product.
     * <p>Cannot be blank.</p>
     */
    @NotBlank(message = "SKU is required")
    private String sku;

    /**
     * The price of the product.
     * <p>Must be a positive decimal value.</p>
     */
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    /**
     * The number of units available in stock.
     * <p>Cannot be null or negative.</p>
     */
    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer stock;

    /**
     * Indicates whether the product is active or available for sale.
     * <p>Defaults to {@code true} if not provided.</p>
     */
    private Boolean activeStatus = true;
}
