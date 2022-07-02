package io.metersphere.security;

import io.metersphere.commons.utils.LogUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(urlPatterns = "/*", filterName = "jettyFilter")
public class JettyFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        LogUtil.debug("拦截器执行-----");
        if ("TRACE".equalsIgnoreCase(httpRequest.getMethod()) || "TRACK".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            LogUtil.info("trace 拦截执行");
            return;
        }
        LogUtil.debug("拦截器结束-----");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}