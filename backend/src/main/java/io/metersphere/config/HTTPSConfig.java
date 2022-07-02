package io.metersphere.config;


import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.ServerConnector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.jetty.ConfigurableJettyWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnProperty(name = "server.ssl.enabled", havingValue = "true")
public class HTTPSConfig implements WebServerFactoryCustomizer<ConfigurableJettyWebServerFactory> {

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

    @Override
    public void customize(ConfigurableJettyWebServerFactory factory) {

        factory.addServerCustomizers(
                server -> {
                    HttpConfiguration httpConfiguration = new HttpConfiguration();
                    httpConfiguration.setSecurePort(httpsPort);
                    httpConfiguration.setSecureScheme("https");

                    ServerConnector connector = new ServerConnector(server);
                    connector.addConnectionFactory(new HttpConnectionFactory(httpConfiguration));
                    connector.setPort(httpPort);
                    server.addConnector(connector);
                }
        );
    }
}

