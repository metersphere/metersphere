package io.metersphere.gateway.filter;

import io.metersphere.commons.constants.SessionConstants;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.RsaKey;
import io.metersphere.commons.utils.RsaUtil;
import io.metersphere.controller.handler.ResultHolder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

@Component
public class AuthFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 放行不是 /is-login 的接口
        if (!exchange.getRequest().getURI().getRawPath().equals("/is-login")) {
            return chain.filter(exchange);
        }

        RsaKey rsaKey = null;
        try {
            rsaKey = RsaUtil.getRsaKey();
        } catch (NoSuchAlgorithmException e) {
        }
        // 从请求头中获取Auth Token
        String authToken = exchange.getRequest().getHeaders().getFirst(SessionConstants.HEADER_TOKEN);
        String csrfToken = exchange.getRequest().getHeaders().getFirst(SessionConstants.CSRF_TOKEN);
        if (authToken == null || csrfToken == null) {
            // 将错误信息转换为JSON格式
            byte[] body = JSON.toJSONString(ResultHolder.error(rsaKey.getPublicKey())).getBytes(StandardCharsets.UTF_8);
            // 设置响应体和响应类型
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(body);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } else {
            return chain.filter(exchange);
        }
    }
}
