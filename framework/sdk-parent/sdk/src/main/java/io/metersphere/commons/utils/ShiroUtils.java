package io.metersphere.commons.utils;

import io.metersphere.security.CustomSessionIdGenerator;
import io.metersphere.security.CustomSessionManager;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;

import java.util.Map;

public class ShiroUtils {

    public static void loadBaseFilterChain(Map<String, String> filterChainDefinitionMap) {

        filterChainDefinitionMap.put("/resource/md/get/**", "anon");
        filterChainDefinitionMap.put("/resource/ui/get/**", "anon");
        filterChainDefinitionMap.put("/attachment/preview/**", "anon");
        filterChainDefinitionMap.put("/*.worker.js", "anon");
        filterChainDefinitionMap.put("/*.html", "anon");
        filterChainDefinitionMap.put("/signin", "anon");
        filterChainDefinitionMap.put("/ldap/signin", "anon");
        filterChainDefinitionMap.put("/ldap/open", "anon");
        filterChainDefinitionMap.put("/signout", "anon");
        filterChainDefinitionMap.put("/is-login", "anon");
        filterChainDefinitionMap.put("/services", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/img/**", "anon");
        filterChainDefinitionMap.put("/assets/**", "anon");
        filterChainDefinitionMap.put("/fonts/**", "anon");
        filterChainDefinitionMap.put("/display/info", "anon");
        filterChainDefinitionMap.put("/favicon.ico", "anon");
        filterChainDefinitionMap.put("/display/file/**", "anon");
        filterChainDefinitionMap.put("/jmeter/download/**", "anon");
        filterChainDefinitionMap.put("/jmeter/ping", "anon");
        filterChainDefinitionMap.put("/jmeter/ready/**", "anon");
        filterChainDefinitionMap.put("/authsource/list/allenable", "anon");
        filterChainDefinitionMap.put("/module/list", "anon");
        filterChainDefinitionMap.put("/sso/signin", "anon");
        filterChainDefinitionMap.put("/sso/callback/**", "anon");
        filterChainDefinitionMap.put("/license/validate", "anon");
        filterChainDefinitionMap.put("/api/jmeter/download", "anon");
        filterChainDefinitionMap.put("/api/jmeter/download/files", "anon");
        filterChainDefinitionMap.put("/api/jmeter/download/jar", "anon");
        filterChainDefinitionMap.put("/api/jmeter/download/plug/jar", "anon");

        // for swagger
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        filterChainDefinitionMap.put("/swagger-ui/**", "anon");
        filterChainDefinitionMap.put("/v3/api-docs/**", "anon");

        filterChainDefinitionMap.put("/403", "anon");
        filterChainDefinitionMap.put("/anonymous/**", "anon");
        filterChainDefinitionMap.put("/eureka/**", "anon");

        //分享相关接口
        filterChainDefinitionMap.put("/share/info/generateShareInfoWithExpired", "anon");
        filterChainDefinitionMap.put("/share/list", "anon");
        filterChainDefinitionMap.put("/share/info/selectHistoryReportById", "anon");
        filterChainDefinitionMap.put("/share/get/**", "anon");
        filterChainDefinitionMap.put("/share/info", "apikey, csrf, authc"); // 需要认证
        filterChainDefinitionMap.put("/document/**", "anon");
        filterChainDefinitionMap.put("/chart-pic/**", "anon");
        filterChainDefinitionMap.put("/share/**", "anon");
        filterChainDefinitionMap.put("/sharePlanReport", "anon");
        filterChainDefinitionMap.put("/share-report", "anon");
        filterChainDefinitionMap.put("/share-document", "anon");
        filterChainDefinitionMap.put("/share-api-report", "anon");
        filterChainDefinitionMap.put("/api/scenario/report/get/**", "anon");
        filterChainDefinitionMap.put("/shareUiReport", "anon");

        filterChainDefinitionMap.put("/system/theme", "anon");
        filterChainDefinitionMap.put("/system/save/baseurl/**", "anon");
        filterChainDefinitionMap.put("/system/timeout", "anon");
        filterChainDefinitionMap.put("/file/metadata/info/**", "anon");

        filterChainDefinitionMap.put("/v1/catalog/**", "anon");
        filterChainDefinitionMap.put("/v1/agent/**", "anon");
        filterChainDefinitionMap.put("/v1/health/**", "anon");
        //mock接口
        filterChainDefinitionMap.put("/mock/**", "anon");
        filterChainDefinitionMap.put("/ws/**", "anon");
        //
        filterChainDefinitionMap.put("/performance/update/cache", "anon");
        // websocket
        filterChainDefinitionMap.put("/websocket/**", "csrf");
    }

    public static void ignoreCsrfFilter(Map<String, String> filterChainDefinitionMap) {
        filterChainDefinitionMap.put("/", "apikey, authc"); // 跳转到 / 不用校验 csrf
        filterChainDefinitionMap.put("/language", "apikey, authc");// 跳转到 /language 不用校验 csrf
        filterChainDefinitionMap.put("/mock", "apikey, authc"); // 跳转到 /mock接口 不用校验 csrf
    }

    public static Cookie getSessionIdCookie() {
        SimpleCookie sessionIdCookie = new SimpleCookie();
        sessionIdCookie.setPath("/");
        sessionIdCookie.setName("MS_SESSION_ID");
        return sessionIdCookie;
    }

    public static SessionManager getSessionManager(Long sessionTimeout, CacheManager cacheManager) {
        DefaultWebSessionManager sessionManager = new CustomSessionManager();
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        sessionManager.setDeleteInvalidSessions(true);
        sessionManager.setSessionValidationSchedulerEnabled(true);
        sessionManager.setSessionIdCookie(ShiroUtils.getSessionIdCookie());
        sessionManager.setGlobalSessionTimeout(sessionTimeout * 1000);// 超时时间ms
        sessionManager.setCacheManager(cacheManager);
        AbstractSessionDAO sessionDAO = (AbstractSessionDAO) sessionManager.getSessionDAO();
        sessionDAO.setSessionIdGenerator(new CustomSessionIdGenerator());

        //sessionManager.setSessionIdCookieEnabled(true);
        return sessionManager;
    }
}
