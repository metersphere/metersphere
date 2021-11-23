package io.metersphere.controller.handler;


import io.metersphere.commons.utils.LogUtil;
import lombok.Builder;
import lombok.Data;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Aspect
@Component
public class WebLogAspect {
    //获取线程副本
    ThreadLocal<Handler> handler = new ThreadLocal<>();

    @Pointcut("execution(public * io.metersphere..*.*Controller.*(..))")
    public void weblog() {
    }

    @Before("weblog()")
    public void doBefore(JoinPoint joinPoint) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        StringBuffer url = request.getRequestURL();

        Handler h = Handler.builder()
                .url(url.toString())
                .host(request.getRemoteAddr())
                .startTime(System.currentTimeMillis())
                .method(request.getMethod())
                .args(joinPoint.getArgs())
                .build();

        handler.set(h);
    }

    @AfterReturning(returning = "ret", pointcut = "weblog()")
    public void doAfter(Object ret) {
        Handler h = handler.get();

        StringBuilder sb = new StringBuilder()
                .append(h.getMethod()).append(": ")
                .append(h.getUrl()).append(" ")
                .append("TIME: ")
                .append((System.currentTimeMillis() - h.getStartTime())).append("ms ");

        if (LogUtil.getLogger().isDebugEnabled()) {
            sb.append("ARGS: ")
                    .append(Arrays.toString(h.getArgs())).append(" ")
                    .append("RETURN: ")
                    .append(ret);
            LogUtil.debug(sb.toString());
        } else {
            LogUtil.info(sb.toString());
        }
    }

    @Data
    @Builder
    static class Handler {
        private long startTime;
        private String host;
        private String url;
        private String method;
        private Object[] args;
    }
}