package io.metersphere.gateway;

import io.metersphere.autoconfigure.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication(exclude = {
        ShiroConfig.class,
        CommonsDatabaseConfig.class,
        RsaConfig.class,
        SessionConfig.class,
        WebSocketConfig.class,
        PermissionConfig.class,
        OpenApiConfig.class,
        MinIOConfig.class,
        ServletWebServerFactoryAutoConfiguration.class
})
@EnableDiscoveryClient
@PropertySource(value = {
        "classpath:commons.properties",
        "file:/opt/metersphere/conf/metersphere.properties",
}, encoding = "UTF-8", ignoreResourceNotFound = true)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
