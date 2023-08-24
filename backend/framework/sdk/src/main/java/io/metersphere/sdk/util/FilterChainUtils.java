package io.metersphere.sdk.util;

import java.util.HashMap;
import java.util.Map;

public class FilterChainUtils {

    public static Map<String, String> loadBaseFilterChain() {
        Map<String, String> filterChainDefinitionMap = new HashMap<>();
        filterChainDefinitionMap.put("/*.html", "anon");
        filterChainDefinitionMap.put("/login", "anon");
        filterChainDefinitionMap.put("/ldap/login", "anon");
        filterChainDefinitionMap.put("/ldap/open", "anon");
        filterChainDefinitionMap.put("/signout", "anon");
        filterChainDefinitionMap.put("/is-login", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/images/**", "anon");
        filterChainDefinitionMap.put("/assets/**", "anon");
        filterChainDefinitionMap.put("/fonts/**", "anon");
        filterChainDefinitionMap.put("/display/info", "anon");
        filterChainDefinitionMap.put("/favicon.ico", "anon");
        filterChainDefinitionMap.put("/base-display/**", "anon");
        filterChainDefinitionMap.put("/jmeter/download/**", "anon");
        filterChainDefinitionMap.put("/jmeter/ping", "anon");
        filterChainDefinitionMap.put("/jmeter/ready/**", "anon");
        filterChainDefinitionMap.put("/authsource/list/allenable", "anon");
        filterChainDefinitionMap.put("/sso/callback/**", "anon");
        filterChainDefinitionMap.put("/license/validate", "anon");
        filterChainDefinitionMap.put("/system/version/current", "anon");

        // for swagger
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        filterChainDefinitionMap.put("/swagger-ui/**", "anon");
        filterChainDefinitionMap.put("/v3/api-docs/**", "anon");

        filterChainDefinitionMap.put("/403", "anon");
        filterChainDefinitionMap.put("/anonymous/**", "anon");

        //分享相关接口

        filterChainDefinitionMap.put("/system/theme", "anon");
        filterChainDefinitionMap.put("/system/save/baseurl/**", "anon");
        filterChainDefinitionMap.put("/system/timeout", "anon");
        filterChainDefinitionMap.put("/file/metadata/info/**", "anon");
        // consul
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

        // 获取插件中的图片
        filterChainDefinitionMap.put("/plugin/image/**", "anon");

        return filterChainDefinitionMap;
    }

    public static Map<String, String> ignoreCsrfFilter() {
        Map<String, String> filterChainDefinitionMap = new HashMap<>();
        filterChainDefinitionMap.put("/", "apikey, authc"); // 跳转到 / 不用校验 csrf
        filterChainDefinitionMap.put("/language", "apikey, authc");// 跳转到 /language 不用校验 csrf
        filterChainDefinitionMap.put("/mock", "apikey, authc"); // 跳转到 /mock接口 不用校验 csrf
        return filterChainDefinitionMap;
    }
}
