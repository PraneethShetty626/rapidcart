package com.rapidcart.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductOrderDto {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private Long customerId;
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return "ProductOrder{" +
                "id=" + id +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", unitPrice=" + unitPrice +
                ", quantity=" + quantity +
                ", totalPrice=" + totalPrice +
                ", customerId=" + customerId +
                ", createdAt=" + createdAt +
                '}';
    }
}
