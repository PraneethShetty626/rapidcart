package com.rapidcart.order_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up OpenAPI (Swagger) documentation
 * for the RapidCart Product Service.
 *
 * <p>This class defines the OpenAPI bean used by Springdoc to automatically
 * generate API documentation and provide a Swagger UI interface.</p>
 *
 * <p>The configuration includes metadata such as the API title, description,
 * and version, which will appear in the Swagger UI and OpenAPI spec.</p>
 *
 * <p>Access the documentation at:
 * <ul>
 *     <li><b>Swagger UI:</b> http://localhost:8080/swagger-ui.html</li>
 *     <li><b>OpenAPI JSON:</b> http://localhost:8080/v3/api-docs</li>
 * </ul>
 * </p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates and configures the OpenAPI definition for the Product Service.
     *
     * @return a customized {@link OpenAPI} instance containing API metadata
     */
    @Bean
    public OpenAPI productServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RapidCart Product Service API")
                        .description("API documentation for the Product Service module of RapidCart")
                        .version("1.0.0"));
    }
}
