package io.metersphere.config;


import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnProperty(name = "server.ssl.enabled", havingValue = "true")
public class HTTPSConfig {

    /**
     * http服务端口
     */
    @Value("${server.http.port}")
    private Integer httpPort;

    /**
     * https服务端口
     */
    @Value("${server.port}")
    private Integer httpsPort;


    @Bean
    public ServletWebServerFactory undertowFactory() {
        UndertowServletWebServerFactory undertowFactory = new UndertowServletWebServerFactory();
        undertowFactory.addBuilderCustomizers((Undertow.Builder builder) -> {
            builder.addHttpListener(httpPort, "0.0.0.0");
            // 开启HTTP2
            builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true);
        });
        // 暂不开启自动跳转
//        undertowFactory.addDeploymentInfoCustomizers(deploymentInfo -> {
//            // 开启HTTP自动跳转至HTTPS
//            deploymentInfo.addSecurityConstraint(new SecurityConstraint()
//                            .addWebResourceCollection(new WebResourceCollection().addUrlPattern("/*"))
//                            .setTransportGuaranteeType(TransportGuaranteeType.CONFIDENTIAL)
//                            .setEmptyRoleSemantic(SecurityInfo.EmptyRoleSemantic.PERMIT))
//                    .setConfidentialPortManager(exchange -> httpsPort);
//        });
        return undertowFactory;
    }

}

