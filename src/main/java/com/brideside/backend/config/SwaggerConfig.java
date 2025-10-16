package com.brideside.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Brideside Backend API")
                        .version("1.0.0")
                        .description("API documentation for Brideside Backend - Deal Management System. Includes two-step deal creation: initialize with contact number, then update with full details.")
                        .contact(new Contact()
                                .name("Brideside Team")
                                .email("support@brideside.com")
                                .url("https://brideside.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.brideside.com")
                                .description("Production Server")
                ));
    }
}
