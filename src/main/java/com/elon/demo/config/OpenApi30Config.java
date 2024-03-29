package com.elon.demo.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApi30Config {

    @Bean
    public OpenAPI customOpenAPI() {
        final String bearerAuth = "bearerToken";

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(bearerAuth))
                .components(new Components().addSecuritySchemes(bearerAuth, new SecurityScheme().name(bearerAuth).type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .info(new Info().title("接口文档"))
                .externalDocs(new ExternalDocumentation().url("/ApiDoc.html").description("接口说明"));
    }
}
