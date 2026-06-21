package com.lautung.phonecar.backend.common;

import com.lautung.phonecar.backend.auth.AdminSessionProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    public static final String BEARER_AUTH = "bearerAuth";
    public static final String ADMIN_REFRESH_COOKIE = "adminRefreshCookie";

    @Bean
    OpenAPI phoneCarOpenApi(AdminSessionProperties sessionProperties) {
        Components components = new Components()
                .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"))
                .addSecuritySchemes(ADMIN_REFRESH_COOKIE, new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name(sessionProperties.cookieName()));
        return new OpenAPI()
                .info(new Info().title("PhoneCar API").version("v1"))
                .components(components)
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
