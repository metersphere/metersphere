package io.metersphere.gateway.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Optional;

@Component
public class SessionFilter implements WebFilter {
    // 所有模块的前缀
    private static final String[] PREFIX = new String[]{"/setting", "/project", "/api", "/performance", "/track", "/workstation", "/ui", "/report"};
    private static final String[] TO_SUB_SERVICE = new String[]{"/license", "/system"};
    private static final String PERFORMANCE_DOWNLOAD_PREFIX = "/jmeter/";
    private static final String API_DOWNLOAD_PREFIX = "/api/jmeter/";

    @Resource
    private DiscoveryClient discoveryClient;
    @Value("${spring.application.name}")
    private String serviceName;

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        String path = req.getURI().getRawPath();

        // 转发 css js 到具体的模块
        if (path.startsWith("/css") || path.startsWith("/js")) {
            for (String prefix : PREFIX) {
                if (path.contains(prefix)) {
                    ServerWebExchangeUtils.addOriginalRequestUrl(exchange, req.getURI());
                    String newPath = prefix + path;
                    ServerHttpRequest request = req.mutate().path(newPath).build();
                    exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, request.getURI());
                    return chain.filter(exchange.mutate().request(request).build());
                }
            }
        }

        // 有些url直接转到 /setting
        for (String prefix : TO_SUB_SERVICE) {
            if (path.startsWith(prefix)) {
                Optional<String> svc = discoveryClient.getServices().stream().filter(s -> !StringUtils.equals(serviceName, s)).findAny();
                if (svc.isEmpty()) {
                    break;
                }
                String service = svc.get();
                ServerWebExchangeUtils.addOriginalRequestUrl(exchange, req.getURI());
                String newPath = "/" + service + "/" + path;
                ServerHttpRequest request = req.mutate().path(newPath).build();
                exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, request.getURI());
                return chain.filter(exchange.mutate().request(request).build());
            }
        }

        // 从当前站点下载资源
        if (path.startsWith(PERFORMANCE_DOWNLOAD_PREFIX)) {
            ServerWebExchangeUtils.addOriginalRequestUrl(exchange, req.getURI());
            String newPath = "/performance" + path;
            ServerHttpRequest request = req.mutate().path(newPath).build();
            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, request.getURI());
            return chain.filter(exchange.mutate().request(request).build());
        }

        if (path.startsWith(API_DOWNLOAD_PREFIX)) {
            ServerWebExchangeUtils.addOriginalRequestUrl(exchange, req.getURI());
            String newPath = "/api" + path;
            ServerHttpRequest request = req.mutate().path(newPath).build();
            exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, request.getURI());
            return chain.filter(exchange.mutate().request(request).build());
        }

        return chain.filter(exchange);
    }
}
