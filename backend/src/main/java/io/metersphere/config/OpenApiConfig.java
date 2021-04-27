package io.metersphere.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
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
    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            if (!"login".equals(handlerMethod.getMethod().getName())) {
                return operation.addParametersItem(new Parameter().in("header").required(true).name("CSRF-TOKEN"));
            }
            return operation;
        };
    }
}
