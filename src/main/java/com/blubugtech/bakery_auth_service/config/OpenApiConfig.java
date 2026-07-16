package com.blubugtech.bakery_auth_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8083}")
    private String serverPort;

    @Bean
        Info info = new Info()
                .title("Bakery Auth Service API")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints to manage authentication and authorization for the Bakery application.")
                .license(mitLicense);

        return new OpenAPI().info(info);
    }
}
