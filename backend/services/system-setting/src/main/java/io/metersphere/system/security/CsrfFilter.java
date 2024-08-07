package io.metersphere.system.security;


import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.CodingUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.utils.SessionUtils;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;

public class CsrfFilter extends AnonymousFilter {

    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);

        if (!SecurityUtils.getSubject().isAuthenticated()) {
            ((HttpServletResponse) response).setHeader(SessionConstants.AUTHENTICATION_STATUS, SessionConstants.AUTHENTICATION_INVALID);
            return true;
        }
        // 错误页面不需要 csrf
        if (WebUtils.toHttp(request).getRequestURI().equals("/error")) {
            return true;
        }
        // api 过来的请求不需要 csrf
        if (ApiKeyHandler.isApiKeyCall(WebUtils.toHttp(request))) {
            return true;
        }
        // websocket 不需要csrf
        String websocketKey = httpServletRequest.getHeader("Sec-WebSocket-Key");
        if (StringUtils.isNotBlank(websocketKey)) {
            return true;
        }

        // 请求头取出的token value
        String csrfToken = httpServletRequest.getHeader(SessionConstants.CSRF_TOKEN);
        String xAuthToken = httpServletRequest.getHeader(SessionConstants.HEADER_TOKEN);
        // 校验 token
        validateToken(csrfToken, xAuthToken);
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

    private void validateToken(String csrfToken, String xAuthToken) {
        if (StringUtils.isBlank(csrfToken)) {
            throw new RuntimeException("csrf token is empty");
        }
        csrfToken = CodingUtils.aesDecrypt(csrfToken, SessionUser.secret, SessionUser.iv);

        String[] signatureArray = StringUtils.split(StringUtils.trimToNull(csrfToken), "|");
        if (signatureArray.length != 4) {
            throw new RuntimeException("invalid token");
        }
        if (!StringUtils.equals(SessionUtils.getUserId(), signatureArray[0])) {
            throw new RuntimeException("Please check csrf token.");
        }
        if (!StringUtils.equals(SessionUtils.getSessionId(), signatureArray[2]) &&
                !StringUtils.equals(xAuthToken, signatureArray[2])) {
            throw new RuntimeException("Please check csrf token.");
        }
    }
}
