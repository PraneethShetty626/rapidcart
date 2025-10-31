package com.rapidcart.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapidcart.product_service.dto.ProductRequestDto;
import com.rapidcart.product_service.entity.Product;
import com.rapidcart.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TestProductController {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;
    private ProductRequestDto testProductDto;

    @BeforeEach
    void setUpDatabase() throws Exception {
        repository.deleteAll();

        // Create test data
        testProduct = Product.builder()
                .name("Test Product")
                .sku("TEST-001")
                .price(new BigDecimal("99.99"))
                .stock(50)
                .activeStatus(true)
                .build();

        testProductDto = ProductRequestDto.builder()
                .name("New Product")
                .sku("NEW-001")
                .price(new BigDecimal("199.99"))
                .stock(25)
                .activeStatus(true)
                .build();
    }

    @Test
    void shouldCreateProductSuccessfully() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.sku").value("NEW-001"))
                .andExpect(jsonPath("$.price").value(199.99))
                .andExpect(jsonPath("$.stock").value(25))
                .andExpect(jsonPath("$.activeStatus").value(true))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingProductWithInvalidData() throws Exception {
        ProductRequestDto invalidProduct = ProductRequestDto.builder()
                .name("") // Invalid: blank name
                .sku("") // Invalid: blank SKU
                .price(new BigDecimal("-10")) // Invalid: negative price
                .stock(-5) // Invalid: negative stock
                .build();

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCreatingProductWithNullValues() throws Exception {
        ProductRequestDto invalidProduct = ProductRequestDto.builder()
                .name("Valid Name")
                .sku("VALID-SKU")
                .price(null) // Invalid: null price
                .stock(null) // Invalid: null stock
                .build();

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAllProductsSuccessfully() throws Exception {
        // Save test products
        Product product1 = repository.save(testProduct);
        Product product2 = repository.save(Product.builder()
                .name("Second Product")
                .sku("TEST-002")
                .price(new BigDecimal("149.99"))
                .stock(30)
                .activeStatus(true)
                .build());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(product1.getId()))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[1].id").value(product2.getId()))
                .andExpect(jsonPath("$[1].name").value("Second Product"));
    }

    @Test
    void shouldGetAllProductsWithPaginationAndSorting() throws Exception {
        // Save multiple test products
        for (int i = 1; i <= 5; i++) {
            repository.save(Product.builder()
                    .name("Product " + i)
                    .sku("SKU-" + String.format("%03d", i))
                    .price(new BigDecimal("100.00").add(new BigDecimal(i)))
                    .stock(10 + i)
                    .activeStatus(true)
                    .build());
        }

        // Test pagination and sorting
        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "3")
                .param("sortBy", "name")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void shouldGetProductByIdSuccessfully() throws Exception {
        Product savedProduct = repository.save(testProduct);

        mockMvc.perform(get("/api/products/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProduct.getId()))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.sku").value("TEST-001"))
                .andExpect(jsonPath("$.price").value(99.99))
                .andExpect(jsonPath("$.stock").value(50))
                .andExpect(jsonPath("$.activeStatus").value(true));
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentProduct() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateProductSuccessfully() throws Exception {
        Product savedProduct = repository.save(testProduct);

        ProductRequestDto updateDto = ProductRequestDto.builder()
                .name("Updated Product")
                .sku("UPDATED-001")
                .price(new BigDecimal("299.99"))
                .stock(75)
                .activeStatus(false)
                .build();

        mockMvc.perform(put("/api/products/{id}", savedProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedProduct.getId()))
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.sku").value("UPDATED-001"))
                .andExpect(jsonPath("$.price").value(299.99))
                .andExpect(jsonPath("$.stock").value(75))
                .andExpect(jsonPath("$.activeStatus").value(false));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentProduct() throws Exception {
        mockMvc.perform(put("/api/products/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingWithInvalidData() throws Exception {
        Product savedProduct = repository.save(testProduct);

        ProductRequestDto invalidUpdate = ProductRequestDto.builder()
                .name("") // Invalid: blank name
                .sku("VALID-SKU")
                .price(new BigDecimal("-10")) // Invalid: negative price
                .stock(10)
                .build();

        mockMvc.perform(put("/api/products/{id}", savedProduct.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldDeleteProductSuccessfully() throws Exception {
        Product savedProduct = repository.save(testProduct);

        mockMvc.perform(delete("/api/products/{id}", savedProduct.getId()))
                .andExpect(status().isNoContent());

        // Verify product is soft deleted (marked as inactive)
        mockMvc.perform(get("/api/products/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeStatus").value(false));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentProduct() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCheckStockSuccessfully() throws Exception {
        Product savedProduct = repository.save(testProduct);

        mockMvc.perform(get("/api/products/{id}/stock", savedProduct.getId())
                .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(savedProduct.getId()))
                .andExpect(jsonPath("$.hasStock").value(true))
                .andExpect(jsonPath("$.availableStock").value(50))
                .andExpect(jsonPath("$.requestedQuantity").value(10));
    }

    @Test
    void shouldReturnFalseWhenCheckingStockForInsufficientQuantity() throws Exception {
        Product savedProduct = repository.save(testProduct);

        mockMvc.perform(get("/api/products/{id}/stock", savedProduct.getId())
                .param("quantity", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(savedProduct.getId()))
                .andExpect(jsonPath("$.hasStock").value(false))
                .andExpect(jsonPath("$.availableStock").value(50))
                .andExpect(jsonPath("$.requestedQuantity").value(100));
    }

    @Test
    void shouldReturnBadRequestWhenCheckingStockWithInvalidQuantity() throws Exception {
        Product savedProduct = repository.save(testProduct);

        mockMvc.perform(get("/api/products/{id}/stock", savedProduct.getId())
                .param("quantity", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenCheckingStockForNonExistentProduct() throws Exception {
        mockMvc.perform(get("/api/products/{id}/stock", 999L)
                .param("quantity", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReduceStockSuccessfully() throws Exception {
        Product savedProduct = repository.save(testProduct);

        mockMvc.perform(put("/api/products/{id}/reduce-stock", savedProduct.getId())
                .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Stock reduced successfully"));

        // Verify stock was reduced
        mockMvc.perform(get("/api/products/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(40));
    }

    @Test
    void shouldReturnConflictWhenReducingStockWithInsufficientQuantity() throws Exception {
        Product savedProduct = repository.save(testProduct);

        mockMvc.perform(put("/api/products/{id}/reduce-stock", savedProduct.getId())
                .param("quantity", "100"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Insufficient stock or product not found"));
    }

    @Test
    void shouldReturnBadRequestWhenReducingStockWithInvalidQuantity() throws Exception {
        Product savedProduct = repository.save(testProduct);

        mockMvc.perform(put("/api/products/{id}/reduce-stock", savedProduct.getId())
                .param("quantity", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenReducingStockForNonExistentProduct() throws Exception {
        mockMvc.perform(put("/api/products/{id}/reduce-stock", 999L)
                .param("quantity", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldHandleConcurrentStockReduction() throws Exception {
        Product savedProduct = repository.save(testProduct);

        // Simulate concurrent stock reductions
        mockMvc.perform(put("/api/products/{id}/reduce-stock", savedProduct.getId())
                .param("quantity", "25"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/products/{id}/reduce-stock", savedProduct.getId())
                .param("quantity", "25"))
                .andExpect(status().isOk());

        // Verify final stock
        mockMvc.perform(get("/api/products/{id}", savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(0));

        // Try to reduce more stock - should fail
        mockMvc.perform(put("/api/products/{id}/reduce-stock", savedProduct.getId())
                .param("quantity", "1"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldCreateMultipleProductsWithUniqueSkus() throws Exception {
        // Create first product
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductDto)))
                .andExpect(status().isCreated());

        // Try to create second product with same SKU - should fail
        ProductRequestDto duplicateSku = ProductRequestDto.builder()
                .name("Different Name")
                .sku("NEW-001") // Same SKU as testProductDto
                .price(new BigDecimal("299.99"))
                .stock(15)
                .activeStatus(true)
                .build();

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateSku)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldGetEmptyListWhenNoProductsExist() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldHandleInvalidPaginationParameters() throws Exception {
        mockMvc.perform(get("/api/products")
                .param("page", "-1")
                .param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}
