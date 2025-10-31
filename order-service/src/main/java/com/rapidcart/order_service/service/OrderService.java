package com.rapidcart.order_service.service;

import com.rapidcart.order_service.client.ProductClient;
import com.rapidcart.order_service.dto.OrderRequestDto;
import com.rapidcart.order_service.dto.OrderResponseDto;
import com.rapidcart.order_service.dto.ProductDto;
import com.rapidcart.order_service.entity.Order;
import com.rapidcart.order_service.exception.InsufficientStockException;
import com.rapidcart.order_service.exception.ProductNotFoundException;
import com.rapidcart.order_service.exception.ResourceNotFoundException;
import com.rapidcart.order_service.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@code OrderService} class is responsible for handling business logic related to orders
 * in the RapidCart application.
 * <p>
 * It manages order creation, retrieval, and persistence, while interacting with the
 * {@link ProductClient} to validate product details and stock availability, and
 * publishing product-related events using {@link ProductEventPublisher}.
 * <p>
 * All methods are annotated with {@link Transactional} to ensure atomic operations,
 * especially during order creation and stock reduction.
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *     <li>Validate product existence and stock before order creation</li>
 *     <li>Persist new orders to the database</li>
 *     <li>Trigger events to notify other services (e.g., inventory updates)</li>
 *     <li>Retrieve orders by ID, customer, or pagination</li>
 * </ul>
 *
 * Example usage:
 * <pre>
 * {@code
 * OrderRequestDto orderRequest = new OrderRequestDto(1L, 2, 1001L);
 * OrderResponseDto orderResponse = orderService.createOrder(orderRequest);
 * }
 * </pre>
 *
 * @author
 * @since 1.0
 */
@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductClient productClient;

    @Autowired
    private ProductEventPublisher eventPublisher;

    /**
     * Creates a new order after validating product details and stock availability.
     * <p>
     * This method performs the following steps:
     * <ol>
     *     <li>Fetches product details using {@link ProductClient#getProduct(Long)}</li>
     *     <li>Checks if sufficient stock is available for the requested quantity</li>
     *     <li>Calculates total order price</li>
     *     <li>Saves the order to the database</li>
     *     <li>Reduces the product stock accordingly</li>
     *     <li>Publishes an event to notify other services of the new order</li>
     * </ol>
     *
     * @param orderRequestDto the order request containing product ID, quantity, and customer ID
     * @return the created {@link OrderResponseDto}
     * @throws ProductNotFoundException if the product does not exist
     * @throws InsufficientStockException if the product has insufficient stock
     */
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
        ProductDto product = productClient.getProduct(orderRequestDto.getProductId());

        if (product == null || !product.getActiveStatus()) {
            throw new ProductNotFoundException("Product not found or unavailable");
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

        // Trigger the event to notify other services about the new order
        eventPublisher.publishProductEvent("ORDER_CREATED", savedOrder);

        return mapToResponseDto(savedOrder);
    }

    /**
     * Converts an {@link Order} entity into an {@link OrderResponseDto}.
     *
     * @param order the order entity to map
     * @return a mapped {@link OrderResponseDto}
     */
    private OrderResponseDto mapToResponseDto(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getProductId(),
                order.getProductName(),
                order.getUnitPrice(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getCustomerId(),
                order.getCreatedAt()
        );
    }

    /**
     * Retrieves an order by its unique ID.
     *
     * @param id the order ID
     * @return the corresponding {@link OrderResponseDto}
     * @throws RuntimeException if the order is not found
     */
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return mapToResponseDto(order);
    }

    /**
     * Retrieves all orders with pagination support.
     *
     * @param pageable the pagination configuration
     * @return a list of {@link OrderResponseDto} representing all paginated orders
     */
    public List<OrderResponseDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all orders placed by a specific customer, sorted by creation date in descending order.
     *
     * @param customerId the unique ID of the customer
     * @return a list of {@link OrderResponseDto} for the given customer
     */
    public List<OrderResponseDto> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }
}
