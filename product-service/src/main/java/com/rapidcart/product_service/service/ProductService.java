package com.rapidcart.product_service.service;

import com.rapidcart.product_service.dto.ProductRequestDto;
import com.rapidcart.product_service.dto.ProductResponseDto;
import com.rapidcart.product_service.entity.Product;
import com.rapidcart.product_service.exception.ResourceNotFoundException;
import com.rapidcart.product_service.repository.ProductRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer responsible for managing {@link Product} entities.
 *
 * <p>This class encapsulates business logic related to product management —
 * including creation, retrieval, updating, logical deletion (soft delete),
 * and stock management operations.</p>
 *
 * <p>All methods are transactional to ensure data consistency and rollback
 * behavior in case of runtime exceptions.</p>
 */
@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Creates and saves a new product in the database.
     *
     * <p>Converts the incoming {@link ProductRequestDto} into a {@link Product} entity,
     * persists it, and then maps the saved entity back to a {@link ProductResponseDto}.</p>
     *
     * @param productRequestDto the product details provided in the request
     * @return the created product as a {@link ProductResponseDto}
     */
    public ProductResponseDto createProduct(@Valid ProductRequestDto productRequestDto) {
        Product product = mapToEntity(productRequestDto);
        Product savedProduct = productRepository.save(product);
        return mapToResponseDto(savedProduct);
    }

    /**
     * Retrieves a paginated and sorted list of all products.
     *
     * @param pageable contains pagination and sorting parameters
     * @return a list of {@link ProductResponseDto} for the requested page
     */
    public List<ProductResponseDto> getAllProducts(Pageable pageable) {
        Page<Product> productsPage = productRepository.findAll(pageable);
        return productsPage.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Fetches a single product by its unique ID.
     *
     * @param id the product ID
     * @return the corresponding {@link ProductResponseDto}
     * @throws ResourceNotFoundException if no product exists with the specified ID
     */
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));
        return mapToResponseDto(product);
    }

    /**
     * Updates an existing product’s details.
     *
     * <p>If the product does not exist, throws {@link ResourceNotFoundException}.</p>
     *
     * @param id the ID of the product to update
     * @param productRequestDto the updated product data
     * @return the updated product as a {@link ProductResponseDto}
     */
    public ProductResponseDto updateProduct(Long id, @Valid ProductRequestDto productRequestDto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));

        existingProduct.setName(productRequestDto.getName());
        existingProduct.setSku(productRequestDto.getSku());
        existingProduct.setPrice(productRequestDto.getPrice());
        existingProduct.setStock(productRequestDto.getStock());
        existingProduct.setActiveStatus(productRequestDto.getActiveStatus());

        Product updatedProduct = productRepository.save(existingProduct);
        return mapToResponseDto(updatedProduct);
    }

    /**
     * Soft deletes (disables) a product instead of physically removing it.
     *
     * <p>This approach maintains referential integrity with other entities like orders.</p>
     *
     * @param id the product ID
     * @throws ResourceNotFoundException if the product is not found
     */
    public void deleteProduct(Long id) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));

        existingProduct.setActiveStatus(false);
        productRepository.save(existingProduct);
    }

    /**
     * Checks whether a product has sufficient stock for the requested quantity.
     *
     * @param id the product ID
     * @param quantity the required quantity
     * @return true if sufficient stock is available, false otherwise
     */
    public boolean hasStock(Long id, Integer quantity) {
        return productRepository.findById(id)
                .map(product -> product.getStock() >= quantity)
                .orElse(false);
    }

    /**
     * Reduces the stock of a product after an order is confirmed.
     *
     * <p>Throws {@link ResourceNotFoundException} if the product does not exist.
     * Returns false if insufficient stock is available.</p>
     *
     * @param id the product ID
     * @param quantity the quantity to deduct
     * @return true if stock was successfully reduced, false otherwise
     */
    public boolean reduceStock(Long id, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (product.getStock() < quantity) {
            return false;
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        return true;
    }

    /**
     * Converts a {@link ProductRequestDto} to a {@link Product} entity.
     *
     * @param dto the DTO containing product details
     * @return a mapped {@link Product} entity ready for persistence
     */
    private Product mapToEntity(ProductRequestDto dto) {
        return Product.builder()
                .name(dto.getName())
                .sku(dto.getSku())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .activeStatus(dto.getActiveStatus() != null ? dto.getActiveStatus() : true)
                .build();
    }

    /**
     * Converts a {@link Product} entity into a {@link ProductResponseDto}.
     *
     * @param product the entity to convert
     * @return the corresponding response DTO
     */
    private ProductResponseDto mapToResponseDto(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .price(product.getPrice())
                .stock(product.getStock())
                .activeStatus(product.getActiveStatus())
                .build();
    }
}
