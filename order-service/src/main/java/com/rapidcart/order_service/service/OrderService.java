package com.rapidcart.order_service.service;


import com.rapidcart.order_service.client.ProductClient;
import com.rapidcart.order_service.dto.OrderRequestDto;
import com.rapidcart.order_service.dto.OrderResponseDto;
import com.rapidcart.order_service.dto.ProductDto;
import com.rapidcart.order_service.entity.Order;
import com.rapidcart.order_service.exception.InsufficientStockException;
import com.rapidcart.order_service.exception.ProductNotFoundException;
import com.rapidcart.order_service.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService  {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private  ProductEventPublisher eventPublisher;


    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
        ProductDto product = productClient.getProduct(orderRequestDto.getProductId());

        if (product == null) {
            throw new ProductNotFoundException("Product not found");
        }

        if (!productClient.checkStockAndValidate(orderRequestDto.getProductId(), orderRequestDto.getQuantity())) {
            throw new InsufficientStockException("Insufficient stock");
        }


        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(orderRequestDto.getQuantity()));


        Order order = Order.builder()
                .productId(orderRequestDto.getProductId())
                .productName(product.getName())
                .unitPrice(product.getPrice())
                .quantity(orderRequestDto.getQuantity())
                .totalPrice(totalPrice)
                .customerId(orderRequestDto.getCustomerId())
                .build();

        Order savedOrder = orderRepository.save(order);

        productClient.reduceStock(orderRequestDto.getProductId(), orderRequestDto.getQuantity());

        // trigger the event to notify other services about the new order
        eventPublisher.publishProductEvent("ORDER_CREATED", savedOrder);

        return mapToResponseDto(savedOrder);
    }


    private OrderResponseDto mapToResponseDto(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getProductId(),
                order.getProductName(),
                order.getUnitPrice(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getCustomerId(),
                order.getCreatedAt());
    }

    @Transactional
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        return mapToResponseDto(order);
    }

    @Transactional
    public List<OrderResponseDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<OrderResponseDto> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

}