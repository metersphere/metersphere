package io.metersphere.autoconfigure;

import io.metersphere.commons.constants.SessionConstants;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;

@OpenAPIDefinition(
        info = @Info(
                title = "${spring.application.name}",
                version = "2.0"
        ),
        servers = @Server(url = "/" + "${spring.application.name}")
)
public class OpenApiConfig {

    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            if (!"login".equals(handlerMethod.getMethod().getName())) {
                return operation
                        .addParametersItem(new Parameter().in("header").required(true).name(SessionConstants.CSRF_TOKEN))
                        .addParametersItem(new Parameter().in("header").required(true).name(SessionConstants.HEADER_TOKEN));
            }
            return operation;
        };
    }
}
