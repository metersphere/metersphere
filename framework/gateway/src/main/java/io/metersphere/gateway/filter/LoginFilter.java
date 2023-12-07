package io.metersphere.gateway.filter;

import io.metersphere.commons.utils.FilterChainUtils;
import io.metersphere.security.ApiKeyHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
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
import java.util.Optional;

@Component
public class LoginFilter implements WebFilter, Ordered {
    private PathPattern basePattern;

    private final List<PathPattern> excludePatterns = new ArrayList<>();

    @Resource
    private SwaggerUiConfigProperties swaggerUiConfigProperties;

    @PostConstruct
    public void init() {
        basePattern = new PathPatternParser().parse("/**");
        // 网关首页
        excludePatterns.add(new PathPatternParser().parse("/"));
        // 认证源查询
        excludePatterns.add(new PathPatternParser().parse("/authsource/*"));

        // 各模块首页
        swaggerUiConfigProperties.getUrls().forEach(v -> excludePatterns.add(new PathPatternParser().parse("/" + v.getName())));

        FilterChainUtils.loadBaseFilterChain()
                .forEach((url, v) -> {
                    excludePatterns.add(new PathPatternParser().parse(url));
                    excludePatterns.add(new PathPatternParser().parse("/*" + url));
                });

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

        if (basePattern.matches(request.getPath().pathWithinApplication())
                && !excludePatterns.stream()
                .anyMatch(pathPattern -> pathPattern.matches(request.getPath().pathWithinApplication()))
        ) {
            return serverWebExchange.getSession()
                    .doOnNext(session -> Optional.ofNullable(session.getAttribute("user"))
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not found session, Please Login again."))
                    )
                    .then(webFilterChain.filter(serverWebExchange));
        } else {
            return webFilterChain.filter(serverWebExchange);
        }
    }


    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
