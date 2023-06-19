package io.metersphere.security;

import io.metersphere.commons.constants.SessionConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.util.WebUtils;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiKeyFilter extends AnonymousFilter {

    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        // 不是apikey的通过
        if (!ApiKeyHandler.isApiKeyCall(httpRequest) && !SecurityUtils.getSubject().isAuthenticated()) {
            // sso 带了token的
            String token = httpRequest.getHeader(SessionConstants.SSO_TOKEN);
            if (StringUtils.isNotBlank(token)) {
                String userId = SSOSessionHandler.validate(httpRequest);
                if (StringUtils.isNotBlank(userId)) {
                    SecurityUtils.getSubject().login(new UsernamePasswordToken(userId, SSOSessionHandler.random));
                }
            }
            return true;
        }

        // apikey 验证
        if (!SecurityUtils.getSubject().isAuthenticated()) {
            String userId = ApiKeyHandler.getUser(WebUtils.toHttp(request));
            if (StringUtils.isNotBlank(userId)) {
                SecurityUtils.getSubject().login(new UsernamePasswordToken(userId, SSOSessionHandler.random));
            }
        }

        if (!SecurityUtils.getSubject().isAuthenticated()) {
            ((HttpServletResponse) response).setHeader(SessionConstants.AUTHENTICATION_STATUS, "invalid");
        }

        return true;
    }

    @Override
    protected void postHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        // sso 带了token的 退出
        String userId = httpRequest.getHeader(SessionConstants.SSO_TOKEN);
        if (StringUtils.isNotBlank(userId) && SecurityUtils.getSubject().isAuthenticated()) {
            SecurityUtils.getSubject().logout();
        }
        // apikey 退出
        if (ApiKeyHandler.isApiKeyCall(WebUtils.toHttp(request)) && SecurityUtils.getSubject().isAuthenticated()) {
            SecurityUtils.getSubject().logout();
        }
    }
}