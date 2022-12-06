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
    private static final String[] TO_SUB_SERVICE = new String[]{"/license", "/system", "/resource", "/sso/callback/logout", "/sso/callback/cas/logout"};
    private static final String PERFORMANCE_DOWNLOAD_PREFIX = "/jmeter/";
    private static final String API_DOWNLOAD_PREFIX = "/api/jmeter/";
    private static final String TRACK_IMAGE_PREFIX = "/resource/md/get/url";

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
                    return addPrefix(prefix, exchange, chain);
                }
            }
        }

        if (path.startsWith(TRACK_IMAGE_PREFIX)) {
            return addPrefix("/track", exchange, chain);
        }

        // 有些url直接转到 sub-service
        for (String prefix : TO_SUB_SERVICE) {
            if (path.startsWith(prefix)) {
                Optional<String> svc = discoveryClient.getServices().stream().filter(s -> !StringUtils.equals(serviceName, s)).findAny();
                if (svc.isEmpty()) {
                    break;
                }
                String service = svc.get();
                return addPrefix("/" + service + "/", exchange, chain);
            }
        }

        // 从当前站点下载资源
        if (path.startsWith(PERFORMANCE_DOWNLOAD_PREFIX)) {
            return addPrefix("/performance", exchange, chain);
        }

        if (path.startsWith(API_DOWNLOAD_PREFIX)) {
            return addPrefix("/api", exchange, chain);
        }

        return chain.filter(exchange);
    }

    private Mono<Void> addPrefix(String prefix, final ServerWebExchange exchange, final WebFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        String path = req.getURI().getRawPath();
        ServerWebExchangeUtils.addOriginalRequestUrl(exchange, req.getURI());
        String newPath = prefix + path;
        ServerHttpRequest request = req.mutate().path(newPath).build();
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, request.getURI());
        return chain.filter(exchange.mutate().request(request).build());
    }
}
