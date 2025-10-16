package com.corsairops.maintenance.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${api-gateway.url}")
    private String apiGatewayUrl;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI(SpecVersion.V31)
                .info(info())
                .servers(List.of(
                        new Server().url(apiGatewayUrl).description("API Gateway")
                ))
                .components(components())
                .addSecurityItem(securityRequirement());
    }

    private Info info() {
        return new Info()
                .title("Maintenance Service API")
                .description("API documentation for the Asset Service")
                .version("1.0.0");
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("bearerAuth");
    }

    private Components components() {
        return new Components()
                .addSecuritySchemes("bearerAuth", bearerSecurityScheme());
    }

    private SecurityScheme bearerSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
    }
}