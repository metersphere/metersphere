package io.metersphere.security;

import io.metersphere.commons.constants.SessionConstants;
import io.metersphere.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class ApiKeyFilter extends AnonymousFilter {

    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
        // 不是apikey的通过
        if (!ApiKeyHandler.isApiKeyCall(WebUtils.toHttp(request))) {
            String id = (String) SecurityUtils.getSubject().getSession().getId();
            // 防止调用时使用 ak 作为 cookie 跳过登入逻辑
            if (id.length() < 20) {
                SecurityUtils.getSubject().logout();
            }
            return true;
        }
        // apikey 验证
        if (!SecurityUtils.getSubject().isAuthenticated()) {
            String userId = ApiKeyHandler.getUser(WebUtils.toHttp(request));
            if (StringUtils.isNotBlank(userId)) {
                if (LogUtil.getLogger().isDebugEnabled()) {
                    LogUtil.getLogger().debug("user auth: " + userId);
                }
                SecurityUtils.getSubject().login(new MsUserToken(userId, ApiKeySessionHandler.random, "LOCAL"));
            }
        }
        // 登录之后验证
        if (!SecurityUtils.getSubject().isAuthenticated()) {
            ((HttpServletResponse) response).setHeader(SessionConstants.AUTHENTICATION_STATUS, SessionConstants.AUTHENTICATION_INVALID);
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
