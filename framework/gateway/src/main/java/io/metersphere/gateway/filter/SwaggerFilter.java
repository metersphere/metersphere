package io.metersphere.gateway.filter;

import io.metersphere.security.ApiKeyHandler;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
public class SwaggerFilter implements WebFilter, Ordered {

    @Value("${swagger.enabled:true}")
    private boolean swaggerEnabled;
    private final List<PathPattern> swaggerPatterns = new ArrayList<>();

    @PostConstruct
    public void init() {

        swaggerPatterns.add(new PathPatternParser().parse("/swagger-ui.html"));
        swaggerPatterns.add(new PathPatternParser().parse("/swagger-ui/**"));
        swaggerPatterns.add(new PathPatternParser().parse("/v3/api-docs/**"));

        swaggerPatterns.add(new PathPatternParser().parse("/*/swagger-ui.html"));
        swaggerPatterns.add(new PathPatternParser().parse("/*/swagger-ui/**"));
        swaggerPatterns.add(new PathPatternParser().parse("/*/v3/api-docs/**"));
    }


    public static Boolean isApiKeyCall(ServerHttpRequest request) {
        if (request == null) {
            return false;
        }

        return StringUtils.isNotBlank(request.getHeaders().getFirst(ApiKeyHandler.API_ACCESS_KEY))
                && StringUtils.isNotBlank(request.getHeaders().getFirst(ApiKeyHandler.API_SIGNATURE));
    }

    @Override
    public Mono<Void> filter(final ServerWebExchange serverWebExchange, final WebFilterChain webFilterChain) {
        ServerHttpRequest request = serverWebExchange.getRequest();
        if (isApiKeyCall(request)) {
            return webFilterChain.filter(serverWebExchange);
        }

        if (swaggerPatterns.stream().anyMatch(pathPattern -> pathPattern.matches(request.getPath().pathWithinApplication()))) {
            if (!swaggerEnabled) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        }
        return webFilterChain.filter(serverWebExchange);
    }


    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
