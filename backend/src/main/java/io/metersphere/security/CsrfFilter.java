package io.metersphere.security;

import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.CodingUtil;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.SessionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CsrfFilter extends AnonymousFilter {
    private static final String TOKEN_NAME = "CSRF-TOKEN";

    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);

        if (!SecurityUtils.getSubject().isAuthenticated()) {
            ((HttpServletResponse) response).setHeader("Authentication-Status", "invalid");
            return true;
        }
        // api 过来的请求
        if (ApiKeyHandler.isApiKeyCall(WebUtils.toHttp(request))) {
            return true;
        }
        // websocket 不需要csrf
        String websocketKey = httpServletRequest.getHeader("Sec-WebSocket-Key");
        if (StringUtils.isNotBlank(websocketKey)) {
            return true;
        }

        // 请求头取出的token value
        String csrfToken = httpServletRequest.getHeader(TOKEN_NAME);
        // 校验 token
        validateToken(csrfToken);
        // 校验 referer
        validateReferer(httpServletRequest);
        return true;
    }

    private void validateReferer(HttpServletRequest request) {
        Environment env = CommonBeanFactory.getBean(Environment.class);
        String domains = env.getProperty("referer.urls");
        if (StringUtils.isBlank(domains)) {
            // 没有配置不校验
            return;
        }

        String[] split = StringUtils.split(domains, ",");
        String referer = request.getHeader(HttpHeaders.REFERER);
        if (split != null) {
            if (!ArrayUtils.contains(split, referer)) {
                throw new RuntimeException("csrf error");
            }
        }
    }

    private void validateToken(String csrfToken) {
        if (StringUtils.isBlank(csrfToken)) {
            throw new RuntimeException("csrf token is empty");
        }
        csrfToken = CodingUtil.aesDecrypt(csrfToken, SessionUser.secret, SessionUser.iv);

        String[] signatureArray = StringUtils.split(StringUtils.trimToNull(csrfToken), "|");
        if (signatureArray.length != 3) {
            throw new RuntimeException("invalid token");
        }

        long signatureTime;
        try {
            signatureTime = Long.parseLong(signatureArray[2]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Environment env = CommonBeanFactory.getBean(Environment.class);
        long timeout = env.getProperty("session.timeout", Long.class, 43200L);
        if (Math.abs(System.currentTimeMillis() - signatureTime) > timeout * 1000) {
            throw new ExpiredCredentialsException("expired token");
        }
        if (!StringUtils.equals(SessionUtils.getUserId(), signatureArray[0])) {
            throw new RuntimeException("Please check csrf token.");
        }
    }
}
