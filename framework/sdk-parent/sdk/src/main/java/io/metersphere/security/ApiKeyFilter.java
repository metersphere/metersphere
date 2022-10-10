package io.metersphere.security;

import io.metersphere.commons.constants.SessionConstants;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ApiKeyFilter extends AnonymousFilter {

    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        // 不是apikey的通过
        if (!ApiKeyHandler.isApiKeyCall(httpRequest) && !SecurityUtils.getSubject().isAuthenticated()) {
            // sso 带了token的
            String userId = ApiKeySessionHandler.validate(httpRequest);
            if (StringUtils.isNotBlank(userId)) {
                SecurityUtils.getSubject().login(new MsUserToken(userId, ApiKeySessionHandler.random, "LOCAL"));
            }
            return true;
        }

        // apikey 验证
        if (!SecurityUtils.getSubject().isAuthenticated()) {
            String userId = ApiKeyHandler.getUser(WebUtils.toHttp(request));
            if (StringUtils.isNotBlank(userId)) {
                SecurityUtils.getSubject().login(new MsUserToken(userId, ApiKeySessionHandler.random, "LOCAL"));
            }
        }

        if (!SecurityUtils.getSubject().isAuthenticated()) {
            ((HttpServletResponse) response).setHeader(SessionConstants.AUTHENTICATION_STATUS, "invalid");
        }

        return true;
    }

    @Override
    protected void postHandle(ServletRequest request, ServletResponse response) throws Exception {
        if (ApiKeyHandler.isApiKeyCall(WebUtils.toHttp(request)) && SecurityUtils.getSubject().isAuthenticated()) {
            SecurityUtils.getSubject().logout();
        }
    }
}