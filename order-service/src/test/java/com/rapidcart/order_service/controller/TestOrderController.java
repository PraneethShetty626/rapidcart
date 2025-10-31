package com.rapidcart.order_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapidcart.order_service.client.ProductClient;
import com.rapidcart.order_service.dto.OrderRequestDto;
import com.rapidcart.order_service.dto.ProductDto;
import com.rapidcart.order_service.entity.Order;
import com.rapidcart.order_service.repository.OrderRepository;
import com.rapidcart.order_service.service.ProductEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TestOrderController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductClient productClient;

    @MockitoBean
    private ProductEventPublisher productEventPublisher;

    private OrderRequestDto testOrderRequest;
    private ProductDto testProduct;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        // Create test data
        testOrderRequest = OrderRequestDto.builder()
                .customerId(1L)
                .productId(101L)
                .quantity(2)
                .build();

        testProduct = ProductDto.builder()
                .id(101L)
                .name("Test Product")
                .sku("TEST-SKU-001")
                .price(new BigDecimal("99.99"))
                .stock(50)
                .activeStatus(true)
                .build();

        testOrder = Order.builder()
                .customerId(1L)
                .productId(101L)
                .productName("Test Product")
                .unitPrice(new BigDecimal("99.99"))
                .quantity(2)
                .totalPrice(new BigDecimal("199.98"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        // Mock product client responses
        when(productClient.getProduct(101L)).thenReturn(testProduct);
        when(productClient.checkStockAndValidate(101L, 2)).thenReturn(true);
        doNothing().when(productClient).reduceStock(101L, 2);
        doNothing().when(productEventPublisher).publishProductEvent(anyString(), any());

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.productId").value(101))
                .andExpect(jsonPath("$.productName").value("Test Product"))
                .andExpect(jsonPath("$.unitPrice").value(99.99))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(199.98))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists());

        // Verify interactions with mocked services
        verify(productClient).getProduct(101L);
        verify(productClient).checkStockAndValidate(101L, 2);
        verify(productClient).reduceStock(101L, 2);
        verify(productEventPublisher).publishProductEvent(anyString(), any());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingOrderWithInvalidData() throws Exception {
        OrderRequestDto invalidOrder = OrderRequestDto.builder()
                .customerId(null) // Invalid: null customer ID
                .productId(101L)
                .quantity(-1) // Invalid: negative quantity
                .build();

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidOrder)))
                .andExpect(status().isBadRequest());

        // Verify no external service calls were made
        verifyNoInteractions(productClient);
        verifyNoInteractions(productEventPublisher);
    }

    @Test
    void shouldReturnBadRequestWhenCreatingOrderWithNullValues() throws Exception {
        OrderRequestDto invalidOrder = OrderRequestDto.builder()
                .customerId(1L)
                .productId(null) // Invalid: null product ID
                .quantity(null) // Invalid: null quantity
                .build();

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidOrder)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productClient);
        verifyNoInteractions(productEventPublisher);
    }

    @Test
    void shouldReturnNotFoundWhenCreatingOrderForNonExistentProduct() throws Exception {
        when(productClient.getProduct(999L))
                .thenThrow(new RuntimeException("Product not found"));

        OrderRequestDto orderForNonExistentProduct = OrderRequestDto.builder()
                .customerId(1L)
                .productId(999L)
                .quantity(1)
                .build();

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderForNonExistentProduct)))
                .andExpect(status().isInternalServerError());

        verify(productClient).getProduct(999L);
        verifyNoMoreInteractions(productClient);
        verifyNoInteractions(productEventPublisher);
    }

    @Test
    void shouldReturnBadRequestWhenInsufficientStock() throws Exception {
        when(productClient.getProduct(101L)).thenReturn(testProduct);
        when(productClient.checkStockAndValidate(101L, 100)).thenReturn(false);

        OrderRequestDto orderWithInsufficientStock = OrderRequestDto.builder()
                .customerId(1L)
                .productId(101L)
                .quantity(100) // More than available stock
                .build();

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderWithInsufficientStock)))
                .andExpect(status().isBadRequest());

        verify(productClient).getProduct(101L);
        verify(productClient).checkStockAndValidate(101L, 100);
        verify(productClient, never()).reduceStock(anyLong(), anyInt());
        verifyNoInteractions(productEventPublisher);
    }

    @Test
    void shouldGetOrderByIdSuccessfully() throws Exception {
        Order savedOrder = orderRepository.save(testOrder);

        mockMvc.perform(get("/api/orders/{id}", savedOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedOrder.getId()))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.productId").value(101))
                .andExpect(jsonPath("$.productName").value("Test Product"))
                .andExpect(jsonPath("$.unitPrice").value(99.99))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(199.98))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentOrder() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetAllOrdersSuccessfully() throws Exception {
        // Save test orders
        Order order1 = orderRepository.save(testOrder);
        Order order2 = orderRepository.save(Order.builder()
                .customerId(2L)
                .productId(102L)
                .productName("Second Product")
                .unitPrice(new BigDecimal("149.99"))
                .quantity(1)
                .totalPrice(new BigDecimal("149.99"))
                .createdAt(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(order1.getId()))
                .andExpect(jsonPath("$[0].productName").value("Test Product"))
                .andExpect(jsonPath("$[1].id").value(order2.getId()))
                .andExpect(jsonPath("$[1].productName").value("Second Product"));
    }

    @Test
    void shouldGetAllOrdersWithPaginationAndSorting() throws Exception {
        // Save multiple test orders
        for (int i = 1; i <= 5; i++) {
            orderRepository.save(Order.builder()
                    .customerId((long) i)
                    .productId((long) (100 + i))
                    .productName("Product " + i)
                    .unitPrice(new BigDecimal("100.00").add(new BigDecimal(i)))
                    .quantity(i)
                    .totalPrice(new BigDecimal("100.00").add(new BigDecimal(i)).multiply(new BigDecimal(i)))
                    .createdAt(LocalDateTime.now().minusMinutes(i))
                    .build());
        }

        // Test pagination and sorting
        mockMvc.perform(get("/api/orders")
                .param("page", "0")
                .param("size", "3")
                .param("sortBy", "customerId")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void shouldGetOrdersByCustomerIdSuccessfully() throws Exception {
        // Save orders for different customers
        Order customerOrder1 = orderRepository.save(testOrder);
        Order customerOrder2 = orderRepository.save(Order.builder()
                .customerId(1L) // Same customer
                .productId(102L)
                .productName("Second Product")
                .unitPrice(new BigDecimal("149.99"))
                .quantity(1)
                .totalPrice(new BigDecimal("149.99"))
                .createdAt(LocalDateTime.now())
                .build());

        // Order for different customer
        orderRepository.save(Order.builder()
                .customerId(2L) // Different customer
                .productId(103L)
                .productName("Third Product")
                .unitPrice(new BigDecimal("199.99"))
                .quantity(1)
                .totalPrice(new BigDecimal("199.99"))
                .createdAt(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/orders/customer/{customerId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[1].customerId").value(1));
    }

    @Test
    void shouldReturnEmptyListWhenCustomerHasNoOrders() throws Exception {
        mockMvc.perform(get("/api/orders/customer/{customerId}", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldGetEmptyListWhenNoOrdersExist() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldHandleInvalidPaginationParameters() throws Exception {
        mockMvc.perform(get("/api/orders")
                .param("page", "-1")
                .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCreateMultipleOrdersForSameCustomer() throws Exception {
        // Mock product client for multiple calls
        when(productClient.getProduct(anyLong())).thenReturn(testProduct);
        when(productClient.checkStockAndValidate(anyLong(), anyInt())).thenReturn(true);
        doNothing().when(productClient).reduceStock(anyLong(), anyInt());
        doNothing().when(productEventPublisher).publishProductEvent(anyString(), any());

        // Create first order
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andExpect(status().isCreated());

        // Create second order for same customer
        OrderRequestDto secondOrder = OrderRequestDto.builder()
                .customerId(1L) // Same customer
                .productId(102L)
                .quantity(1)
                .build();

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondOrder)))
                .andExpect(status().isCreated());

        // Verify both orders exist for the customer
        mockMvc.perform(get("/api/orders/customer/{customerId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void shouldCalculateTotalPriceCorrectly() throws Exception {
        ProductDto expensiveProduct = ProductDto.builder()
                .id(201L)
                .name("Expensive Product")
                .sku("EXP-001")
                .price(new BigDecimal("999.99"))
                .stock(10)
                .activeStatus(true)
                .build();

        when(productClient.getProduct(201L)).thenReturn(expensiveProduct);
        when(productClient.checkStockAndValidate(201L, 3)).thenReturn(true);
        doNothing().when(productClient).reduceStock(201L, 3);
        doNothing().when(productEventPublisher).publishProductEvent(anyString(), any());

        OrderRequestDto expensiveOrder = OrderRequestDto.builder()
                .customerId(1L)
                .productId(201L)
                .quantity(3)
                .build();

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expensiveOrder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.unitPrice").value(999.99))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.totalPrice").value(2999.97)); // 999.99 * 3
    }
}
