package com.rapidcart.order_service.client;


import com.rapidcart.order_service.dto.ProductDto;
import com.rapidcart.order_service.exception.InsufficientStockException;
import com.rapidcart.order_service.exception.ProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class ProductClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${product.service.url:http://localhost:8081}")
    private String productServiceUrl;

    public ProductDto getProduct(Long productId) {
        try {
            String url = productServiceUrl + "/api/products/" + productId;
            ResponseEntity<ProductDto> response = restTemplate.getForEntity(url, ProductDto.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException("Product not found");
        } catch (Exception e) {
            throw new RuntimeException("Error fetching product: " + e.getMessage());
        }
    }

    public boolean checkStockAndValidate(Long productId, Integer quantity) {
        try {
            String url = productServiceUrl + "/api/products/" + productId + "/stock?quantity=" + quantity;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("hasStock")) {
                Boolean hasStock = (Boolean) body.get("hasStock");
                return hasStock != null && hasStock;
            }
            return false;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException("Product not found");
        } catch (Exception e) {
            throw new RuntimeException("Error checking stock: " + e.getMessage());
        }
    }

    public void reduceStock(Long productId, Integer quantity) {
        try {
            String url = productServiceUrl + "/api/products/" + productId + "/reduce-stock?quantity=" + quantity;
            restTemplate.put(url, null);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new InsufficientStockException("Insufficient stock");
        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException("Product not found");
        } catch (Exception e) {
            throw new RuntimeException("Error reducing stock: " + e.getMessage());
        }
    }
}
