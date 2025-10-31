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

/**
 * The {@code ProductClient} class serves as a REST client for interacting with
 * the Product Service within the RapidCart application.
 * <p>
 * It provides functionality to:
 * <ul>
 *     <li>Fetch product details by ID</li>
 *     <li>Validate and check if sufficient stock is available</li>
 *     <li>Reduce stock quantity after a successful order</li>
 * </ul>
 * <p>
 * This class uses {@link RestTemplate} for RESTful communication and
 * handles specific exceptions from the Product Service gracefully.
 * <p>
 * The base URL for the Product Service can be configured via the
 * {@code product.service.url} property (defaults to {@code http://localhost:8081}).
 *
 * <p><b>Exception Handling:</b></p>
 * <ul>
 *     <li>{@link ProductNotFoundException} – Thrown if the product does not exist.</li>
 *     <li>{@link InsufficientStockException} – Thrown if there is not enough stock.</li>
 *     <li>{@link RuntimeException} – Thrown for unexpected or connectivity issues.</li>
 * </ul>
 *
 * Example usage:
 * <pre>
 * {@code
 * ProductDto product = productClient.getProduct(1L);
 * if (productClient.checkStockAndValidate(1L, 5)) {
 *     productClient.reduceStock(1L, 5);
 * }
 * }
 * </pre>
 *
 * @author
 * @since 1.0
 */
@Component
public class ProductClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${product.service.url:http://localhost:8081}")
    private String productServiceUrl;

    /**
     * Fetches product details from the Product Service for the given product ID.
     *
     * @param productId the unique identifier of the product
     * @return the {@link ProductDto} containing product details
     * @throws ProductNotFoundException if the product does not exist
     * @throws RuntimeException if any other error occurs during communication
     */
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

    /**
     * Validates if the given product has sufficient stock available for the requested quantity.
     * <p>
     * Makes a GET request to the Product Service's stock validation endpoint.
     *
     * @param productId the unique identifier of the product
     * @param quantity the desired quantity to check
     * @return {@code true} if sufficient stock exists, {@code false} otherwise
     * @throws ProductNotFoundException if the product does not exist
     * @throws RuntimeException if any other error occurs during communication
     */
    public boolean checkStockAndValidate(Long productId, Integer quantity) {
        try {
            String url = productServiceUrl + "/api/products/" + productId + "/stock?quantity=" + quantity;
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {});

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

    /**
     * Reduces the stock of a product after an order is placed successfully.
     * <p>
     * Makes a PUT request to the Product Service's stock reduction endpoint.
     *
     * @param productId the unique identifier of the product
     * @param quantity the quantity to be reduced from available stock
     * @throws ProductNotFoundException if the product does not exist
     * @throws InsufficientStockException if the available stock is insufficient
     * @throws RuntimeException if any other error occurs during communication
     */
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
