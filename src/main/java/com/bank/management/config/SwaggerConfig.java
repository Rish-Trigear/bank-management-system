package com.bank.management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for API documentation
 * 
 * This class sets up Swagger UI for interactive API documentation.
 * Access at: http://localhost:8080/swagger-ui.html
 * Includes security scheme for employee authentication.
 */
@Configuration
public class SwaggerConfig {
    
    /**
     * Custom OpenAPI configuration for Swagger documentation
     * Defines API info, security schemes, and authentication requirements
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Management System API")
                        .version("1.0")
                        .description("REST API for Bank Management System with employee authentication")
                        .contact(new Contact()
                                .name("Bank Management Team")
                                .email("support@bank.com")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth")) // Apply security globally
                .components(new Components()
                        .addSecuritySchemes("basicAuth", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("basic")
                                        .description("HTTP Basic Authentication. Use employee credentials (email:password)")));
    }
}
