package io.metersphere.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "MeterSphere",
                version = "1.0"
        ),
        servers = @Server(url = "/")
)
@Configuration
public class OpenApiConfig {
}
