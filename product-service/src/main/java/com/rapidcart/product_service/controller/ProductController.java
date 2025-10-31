package com.rapidcart.product_service.controller;

import com.rapidcart.product_service.dto.ProductRequestDto;
import com.rapidcart.product_service.dto.ProductResponseDto;
import com.rapidcart.product_service.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing products within the RapidCart platform.
 *
 * <p>This controller provides CRUD endpoints for managing products, along with
 * internal APIs for stock verification and stock reduction during order processing.</p>
 *
 * <p><b>Base URL:</b> {@code /api/products}</p>
 *
 * <h3>Supported Endpoints</h3>
 * <ul>
 *   <li><b>POST</b> /api/products → Create a new product</li>
 *   <li><b>GET</b> /api/products → Retrieve paginated list of products</li>
 *   <li><b>GET</b> /api/products/{id} → Fetch a product by ID</li>
 *   <li><b>PUT</b> /api/products/{id} → Update product details</li>
 *   <li><b>DELETE</b> /api/products/{id} → Soft delete (deactivate) a product</li>
 *   <li><b>GET</b> /api/products/{id}/stock → Check stock availability (for Order Service)</li>
 *   <li><b>PUT</b> /api/products/{id}/reduce-stock → Reduce stock after confirmed order</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Creates a new product record.
     *
     * @param productRequestDto contains product details such as name, SKU, price, and stock
     * @return a {@link ResponseEntity} containing the created product and HTTP 201 (Created)
     */
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestBody ProductRequestDto productRequestDto) {

        ProductResponseDto createdProduct = productService.createProduct(productRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    /**
     * Retrieves a paginated and optionally sorted list of products.
     *
     * @param page    the page number (0-based, default = 0)
     * @param size    the page size (default = 10)
     * @param sortBy  the field name to sort by (default = "id")
     * @param sortDir the sort direction ("asc" or "desc", default = "asc")
     * @return a {@link ResponseEntity} containing a list of {@link ProductResponseDto} and HTTP 200 (OK)
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        List<ProductResponseDto> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Fetches a single product by its unique identifier.
     *
     * @param id the product ID
     * @return a {@link ResponseEntity} containing the product details and HTTP 200 (OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Updates product details for an existing record.
     *
     * @param id the ID of the product to update
     * @param productRequestDto the updated product data
     * @return a {@link ResponseEntity} containing the updated {@link ProductResponseDto} and HTTP 200 (OK)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDto productRequestDto
    ) {
        ProductResponseDto updatedProduct = productService.updateProduct(id, productRequestDto);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Performs a soft delete by marking a product as inactive instead of removing it from the database.
     *
     * <p>This ensures referential integrity with existing orders or related entities.</p>
     *
     * @param id the product ID to deactivate
     * @return a {@link ResponseEntity} with HTTP 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Checks stock availability for a given product.
     *
     * <p>Primarily used by the Order Service to validate order requests.</p>
     *
     * @param id       the product ID
     * @param quantity the requested quantity (must be >= 1)
     * @return a {@link ResponseEntity} containing availability information and HTTP 200 (OK)
     */
    @GetMapping("/{id}/stock")
    public ResponseEntity<Map<String, Object>> checkStock(
            @PathVariable Long id,
            @NotNull @RequestParam @Min(1) Integer quantity
    ) {
        ProductResponseDto product = productService.getProductById(id);
        boolean hasStock = productService.hasStock(id, quantity);

        Map<String, Object> response = Map.of(
                "productId", id,
                "hasStock", hasStock,
                "availableStock", hasStock ? product.getStock() : 0,
                "requestedQuantity", quantity
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Reduces product stock after a confirmed order is processed.
     *
     * <p>If insufficient stock exists, the response will include an HTTP 409 (Conflict) status.</p>
     *
     * @param id       the product ID
     * @param quantity the quantity to deduct (must be >= 1)
     * @return a {@link ResponseEntity} with success or conflict message
     */
    @PutMapping("/{id}/reduce-stock")
    public ResponseEntity<Map<String, Object>> reduceStock(
            @PathVariable Long id,
            @NotNull @RequestParam @Min(1) Integer quantity
    ) {
        boolean success = productService.reduceStock(id, quantity);

        if (!success) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Insufficient stock or product not found"));
        }

        return ResponseEntity.ok(Map.of("message", "Stock reduced successfully"));
    }
}
