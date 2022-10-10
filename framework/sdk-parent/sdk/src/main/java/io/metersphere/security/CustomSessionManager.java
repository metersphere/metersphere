package io.metersphere.security;

import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

public class CustomSessionManager extends DefaultWebSessionManager {

    static final ThreadLocal<String> threadSessionId = new ThreadLocal<>();

    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        String id = null;
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        if (ApiKeyHandler.isApiKeyCall(httpRequest)) {
            // API调用同一个ak使用同一个session，避免调用频繁，导致session过多，内存泄漏
            id = httpRequest.getHeader(ApiKeyHandler.API_ACCESS_KEY);
            setSessionIdCookieEnabled(false);
            threadSessionId.set(id);
            return id;
        }
        // 线程池中线程可能会复用，非api删除
        threadSessionId.remove();
        setSessionIdCookieEnabled(true);
        return super.getSessionId(request, response);
    }
}
