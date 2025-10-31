package com.rapidcart.order_service.dto;

import com.rapidcart.order_service.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEvent {
    private String eventType;
    private Order data;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ProductEvent(String eventType, Order data) {
        this.eventType = eventType;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
}