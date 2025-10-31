package com.rapidcart.order_service.controller;

import com.rapidcart.order_service.dto.OrderRequestDto;
import com.rapidcart.order_service.dto.OrderResponseDto;
import com.rapidcart.order_service.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The {@code OrderController} class exposes RESTful APIs for managing customer orders
 * in the RapidCart application.
 * <p>
 * It provides endpoints for:
 * <ul>
 *     <li>Creating a new order</li>
 *     <li>Fetching an order by its ID</li>
 *     <li>Retrieving all orders with pagination and sorting</li>
 *     <li>Fetching all orders placed by a specific customer</li>
 * </ul>
 * <p>
 * The controller delegates all business logic to the {@link OrderService}, ensuring
 * a clean separation between web and service layers.
 *
 * <p><b>Base Path:</b> {@code /api/orders}</p>
 *
 * Example usage (HTTP requests):
 * <pre>
 * POST   /api/orders
 * GET    /api/orders/{id}
 * GET    /api/orders?page=0&size=10&sortBy=id&sortDir=asc
 * GET    /api/orders/customer/{customerId}
 * </pre>
 *
 * @author
 * @since 1.0
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Creates a new order for a given customer and product.
     * <p>
     * The request body must include product ID, quantity, and customer ID.
     * Validation is applied to ensure that required fields are provided and valid.
     *
     * @param orderRequestDto the request payload containing order details
     * @return a {@link ResponseEntity} containing the created {@link OrderResponseDto}
     *         and HTTP status {@code 201 Created}
     *
     * <p><b>Possible Errors:</b></p>
     * <ul>
     *     <li>{@code 404 Not Found} – Product does not exist</li>
     *     <li>{@code 400 Bad Request} – Insufficient stock or invalid input</li>
     * </ul>
     */
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto orderRequestDto) {
        OrderResponseDto order = orderService.createOrder(orderRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * Retrieves an order by its unique identifier.
     *
     * @param id the ID of the order to retrieve
     * @return a {@link ResponseEntity} containing the {@link OrderResponseDto}
     *         if found, or an error response if not found
     *
     * <p><b>Possible Errors:</b></p>
     * <ul>
     *     <li>{@code 404 Not Found} – Order does not exist</li>
     * </ul>
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(@PathVariable Long id) {
        OrderResponseDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Retrieves all orders with optional pagination and sorting.
     * <p>
     * Clients can specify page number, page size, sorting field, and sorting direction.
     *
     * @param page the page number (default: 0)
     * @param size the number of records per page (default: 10)
     * @param sortBy the field to sort by (default: "id")
     * @param sortDir the sorting direction ("asc" or "desc", default: "asc")
     * @return a {@link ResponseEntity} containing a list of {@link OrderResponseDto}
     */
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getAllOrders(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        List<OrderResponseDto> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * Retrieves all orders placed by a specific customer, sorted by creation date (latest first).
     *
     * @param customerId the unique ID of the customer
     * @return a {@link ResponseEntity} containing a list of {@link OrderResponseDto}
     *         associated with the given customer
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponseDto>> getOrdersByCustomerId(@PathVariable Long customerId) {
        List<OrderResponseDto> orders = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }
}
