package io.metersphere.commons.utils;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;

import java.util.Map;

public class ShiroUtils {

    public static void loadBaseFilterChain(Map<String, String> filterChainDefinitionMap){

        filterChainDefinitionMap.put("/resource/**", "anon");
        filterChainDefinitionMap.put("/*.worker.js", "anon");
        filterChainDefinitionMap.put("/login", "anon");
        filterChainDefinitionMap.put("/signin", "anon");
        filterChainDefinitionMap.put("/ldap/signin", "anon");
        filterChainDefinitionMap.put("/ldap/open", "anon");
        filterChainDefinitionMap.put("/signout", "anon");
        filterChainDefinitionMap.put("/isLogin", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/img/**", "anon");
        filterChainDefinitionMap.put("/fonts/**", "anon");
        filterChainDefinitionMap.put("/display/info", "anon");
        filterChainDefinitionMap.put("/favicon.ico", "anon");
        filterChainDefinitionMap.put("/display/file/**", "anon");
        filterChainDefinitionMap.put("/jmeter/download/**", "anon");
        filterChainDefinitionMap.put("/jmeter/ping", "anon");
        filterChainDefinitionMap.put("/jmeter/ready/**", "anon");
        filterChainDefinitionMap.put("/authsource/list/allenable", "anon");
        filterChainDefinitionMap.put("/sso/signin", "anon");
        filterChainDefinitionMap.put("/sso/callback", "anon");
        filterChainDefinitionMap.put("/license/valid", "anon");
        filterChainDefinitionMap.put("/api/jmeter/download", "anon");
        filterChainDefinitionMap.put("/api/jmeter/download/jar", "anon");

        // for swagger
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        filterChainDefinitionMap.put("/swagger-ui/**", "anon");
        filterChainDefinitionMap.put("/v3/api-docs/**", "anon");

        filterChainDefinitionMap.put("/403", "anon");
        filterChainDefinitionMap.put("/anonymous/**", "anon");

        //api-对外文档页面提供的查询接口
        filterChainDefinitionMap.put("/api/document/**", "anon");
        filterChainDefinitionMap.put("/document/**", "anon");
        filterChainDefinitionMap.put("/system/theme", "anon");
        filterChainDefinitionMap.put("/system/save/baseurl/**", "anon");
        filterChainDefinitionMap.put("/system/timeout", "anon");

        filterChainDefinitionMap.put("/v1/catalog/**", "anon");
        filterChainDefinitionMap.put("/v1/agent/**", "anon");
        filterChainDefinitionMap.put("/v1/health/**", "anon");
        //mock接口
        filterChainDefinitionMap.put("/mock/**", "anon");
    }

    public static void ignoreCsrfFilter(Map<String, String> filterChainDefinitionMap) {
        filterChainDefinitionMap.put("/", "apikey, authc"); // 跳转到 / 不用校验 csrf
        filterChainDefinitionMap.put("/language", "apikey, authc");// 跳转到 /language 不用校验 csrf
        filterChainDefinitionMap.put("/document", "apikey, authc"); // 跳转到 /document 不用校验 csrf
        filterChainDefinitionMap.put("/test/case/file/preview/**", "apikey, authc"); // 预览测试用例附件 不用校验 csrf
        filterChainDefinitionMap.put("/mock", "apikey, authc"); // 跳转到 /mock接口 不用校验 csrf
        filterChainDefinitionMap.put("/resource/md/get/**", "apikey, authc");
    }

    public static Cookie getSessionIdCookie(){
        SimpleCookie sessionIdCookie = new SimpleCookie();
        sessionIdCookie.setPath("/");
        sessionIdCookie.setName("MS_SESSION_ID");
        return sessionIdCookie;
    }

    public static SessionManager getSessionManager(Long sessionTimeout, CacheManager cacheManager){
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        sessionManager.setDeleteInvalidSessions(true);
        sessionManager.setSessionValidationSchedulerEnabled(true);
        sessionManager.setSessionIdCookie(ShiroUtils.getSessionIdCookie());
        sessionManager.setGlobalSessionTimeout(sessionTimeout * 1000);// 超时时间ms
        sessionManager.setCacheManager(cacheManager);

        //sessionManager.setSessionIdCookieEnabled(true);
        return sessionManager;
    }
}
