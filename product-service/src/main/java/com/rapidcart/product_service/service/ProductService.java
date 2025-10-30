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
 * <p>
 * This class provides transactional methods for creating, retrieving,
 * updating, and logically deleting (disabling) products.
 * </p>
 * <p>
 * All methods are transactional to ensure data consistency.
 * </p>
 */
@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Creates and saves a new product in the database.
     * <p>
     * Validates the incoming {@link ProductRequestDto}, maps it to a {@link Product} entity,
     * and persists it using {@link ProductRepository}.
     * </p>
     *
     * @param productRequestDto the incoming product data from the API request
     * @return the saved product represented as a {@link ProductResponseDto}
     */
    public ProductResponseDto createProduct(@Valid ProductRequestDto productRequestDto) {
        Product product = mapToEntity(productRequestDto);
        Product savedProduct = productRepository.save(product);
        return mapToResponseDto(savedProduct);
    }

    /**
     * Retrieves a paginated and optionally sorted list of products.
     *
     * @param pageable pagination and sorting parameters (page number, size, sort direction)
     * @return a list of {@link ProductResponseDto} objects representing the requested page of products
     */
    public List<ProductResponseDto> getAllProducts(Pageable pageable) {
        Page<Product> productsPage = productRepository.findAll(pageable);
        return productsPage.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a product by its unique identifier.
     *
     * @param id the ID of the product to fetch
     * @return a {@link ProductResponseDto} containing the product details
     * @throws ResourceNotFoundException if no product exists with the given ID
     */
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));
        return mapToResponseDto(product);
    }

    /**
     * Updates an existing product identified by its ID.
     * <p>
     * If the product does not exist, a {@link ResourceNotFoundException} is thrown.
     * </p>
     *
     * @param id                 the ID of the product to update
     * @param productRequestDto  the updated product data
     * @return the updated product represented as a {@link ProductResponseDto}
     * @throws ResourceNotFoundException if no product exists with the given ID
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
     * Logically deletes (disables) a product by its ID instead of physically removing it.
     * <p>
     * This is useful when product records are linked to other entities (e.g., orders)
     * and cannot be safely deleted without violating constraints.
     * </p>
     *
     * @param id the ID of the product to deactivate
     * @throws ResourceNotFoundException if no product exists with the given ID
     */
    public void deleteProduct(Long id) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found"));

        existingProduct.setActiveStatus(false);
        productRepository.save(existingProduct);
    }

    /**
     * Maps a {@link ProductRequestDto} to a {@link Product} entity.
     * <p>
     * This method prepares the entity for persistence, ensuring that
     * default values (like active status) are properly set.
     * </p>
     *
     * @param dto the DTO containing product data from the client
     * @return a mapped {@link Product} entity
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
     * Maps a {@link Product} entity to a {@link ProductResponseDto}.
     * <p>
     * Used to transform entity objects into a format suitable for API responses.
     * </p>
     *
     * @param product the entity object to map
     * @return the corresponding {@link ProductResponseDto}
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
