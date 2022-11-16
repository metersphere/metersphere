package io.metersphere.gateway.filter;

import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

@Component
public class HtmlResourceFilter implements GlobalFilter, Ordered {

    private static final String HTTP_SCHEME = "http";
    private static final HashMap<String, String> NoCacheHeaders = new HashMap<>();

    static {
        NoCacheHeaders.put(HttpHeaders.CACHE_CONTROL, "no-cache");
        NoCacheHeaders.put("Cache", "no-cache");
        NoCacheHeaders.put(HttpHeaders.PRAGMA, "no-cache");
        NoCacheHeaders.put(HttpHeaders.EXPIRES, "0");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    URI requestUrl = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);
                    if (!StringUtils.startsWithIgnoreCase(requestUrl.getScheme(), HTTP_SCHEME)) {
                        // maybe ws or wss instead of http?
                        return;
                    }
                    try {
                        ServerHttpResponse response = exchange.getResponse();
                        if (MediaType.TEXT_HTML.equals(response.getHeaders().getContentType()) && HttpStatus.OK.equals(response.getStatusCode())) {
                            response.getHeaders().setAll(NoCacheHeaders);
                        }
                    } catch (Exception e) {
                        LogUtil.error("Fail to handle url " + requestUrl.getPath(), e);
                    }
                })
        );
    }

    @Override
    public int getOrder() {
        return 99;
    }
}
