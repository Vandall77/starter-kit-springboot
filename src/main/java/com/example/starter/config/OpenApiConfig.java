package com.example.starter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI starterOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Starter Kit Spring Boot API")
                        .description("Minimal Auth + Role/Permission (No Flyway)")
                        .version("v3"));
    }
}
