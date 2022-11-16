package io.metersphere.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;

@Component
public class HtmlResourceFilter implements GatewayFilter, Ordered {

    private static final HashMap<String, String> NoCacheHeaders = new HashMap<>();

    static {
        NoCacheHeaders.put(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        NoCacheHeaders.put(HttpHeaders.PRAGMA, "no-cache");
        NoCacheHeaders.put(HttpHeaders.EXPIRES, "0");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    if (exchange.getRequest().getMethod() == HttpMethod.GET && response.getHeaders().getContentType() == MediaType.TEXT_HTML) {
                        response.getHeaders().setAll(NoCacheHeaders);
                    }
                })
        );
    }

    @Override
    public int getOrder() {
        return 99;
    }
}
