package com.elon.mars.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OpenApi30Config {

    @Bean
    public OpenAPI customOpenAPI() {
        final String bearerAuth = "bearerToken";

        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag().name("租户资源"));
        tags.add(new Tag().name("用户资源"));
        tags.add(new Tag().name("部门资源"));
        tags.add(new Tag().name("角色资源"));
        tags.add(new Tag().name("权限资源"));

        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(bearerAuth))
                .components(new Components()
                        .addSecuritySchemes(bearerAuth, new SecurityScheme()
                                .name(bearerAuth)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")))
                .info(new Info().title("接口文档").version("1"))
                .tags(tags)
                .externalDocs(new ExternalDocumentation()
                        .url("/ApiDoc.html")
                        .description("接口说明"));
    }
}
