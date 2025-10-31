package com.rapidcart.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEvent {
    private String eventType;
    private ProductOrderDto data;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ProductEvent(String eventType, ProductOrderDto data) {
        this.eventType = eventType;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
}