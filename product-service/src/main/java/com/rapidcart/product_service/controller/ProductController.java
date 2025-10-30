package com.rapidcart.product_service.controller;

import com.rapidcart.product_service.dto.ProductRequestDto;
import com.rapidcart.product_service.dto.ProductResponseDto;
import com.rapidcart.product_service.service.ProductService;
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
 * REST controller for managing products in the RapidCart system.
 *
 * <p>This controller exposes endpoints for creating and retrieving products.
 * It supports pagination, sorting, and request validation to ensure consistent and
 * efficient API behavior.</p>
 *
 * <p>Base URL: <b>/api/products</b></p>
 *
 * <p>Endpoints:</p>
 * <ul>
 *   <li><b>POST /api/products</b> - Create a new product</li>
 *   <li><b>GET /api/products</b> - Retrieve all products with pagination and sorting</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 *     POST /api/products
 *     {
 *         "name": "Smartphone",
 *         "price": 499.99,
 *         "description": "Latest model with advanced features"
 *     }
 *
 *     GET /api/products?page=0&size=10&sortBy=name&sortDir=asc
 * </pre>
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Creates a new product in the system.
     *
     * @param productRequestDto the request payload containing product details
     * @return a {@link ResponseEntity} containing the created product details and HTTP 201 (Created) status
     */
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestBody ProductRequestDto productRequestDto) {

        ProductResponseDto createdProduct = productService.createProduct(productRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    /**
     * Retrieves all products with pagination and sorting support.
     *
     * @param page    the page number (zero-based index, default = 0)
     * @param size    the number of records per page (default = 10)
     * @param sortBy  the field name to sort by (default = "id")
     * @param sortDir the sort direction, either "asc" or "desc" (default = "asc")
     * @return a {@link ResponseEntity} containing a list of {@link ProductResponseDto}
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = Sort.by(sortBy);
        if (sortDir.equalsIgnoreCase("desc")) {
            sort = sort.descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        List<ProductResponseDto> products = productService.getAllProducts(pageable);

        return ResponseEntity.ok(products);
    }

    /**
     * Retrieves a specific product by its unique identifier.
     *
     * <p>This endpoint fetches product details for the provided {@code id}.
     * If the product does not exist, a {@link com.rapidcart.product_service.exception.ResourceNotFoundException}
     * is thrown and handled globally.</p>
     *
     * @param id the unique identifier of the product
     * @return a {@link ResponseEntity} containing the {@link ProductResponseDto} and HTTP 200 (OK) status
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Updates the details of an existing product.
     *
     * <p>This endpoint updates an existing product identified by {@code id} with the provided
     * {@link ProductRequestDto} data. Validation is applied to ensure request integrity.
     * If the product does not exist, a {@link com.rapidcart.product_service.exception.ResourceNotFoundException} is thrown.</p>
     *
     * @param id the unique identifier of the product to update
     * @param productRequestDto the updated product data
     * @return a {@link ResponseEntity} containing the updated {@link ProductResponseDto} and HTTP 200 (OK) status
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
     * Deletes (deactivates) a product by its unique identifier.
     *
     * <p>This endpoint performs a soft delete by marking the product as inactive
     * instead of removing it from the database. This approach helps maintain data integrity
     * for orders or references linked to the product.</p>
     *
     * @param id the unique identifier of the product to delete
     * @return a {@link ResponseEntity} with HTTP 204 (No Content) status upon successful deactivation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

}
