package io.metersphere.sdk.util;

import java.util.HashMap;
import java.util.Map;

public class FilterChainUtils {

    public static Map<String, String> loadBaseFilterChain() {
        Map<String, String> filterChainDefinitionMap = new HashMap<>();
        filterChainDefinitionMap.put("/*.html", "anon");
        filterChainDefinitionMap.put("/login", "anon");
        filterChainDefinitionMap.put("/ldap/login", "anon");
        filterChainDefinitionMap.put("/authentication/get-list", "anon");
        filterChainDefinitionMap.put("/authentication/get/by/type/**", "anon");
        filterChainDefinitionMap.put("/we_com/info", "anon");
        filterChainDefinitionMap.put("/ding_talk/info", "anon");
        filterChainDefinitionMap.put("/lark/info", "anon");
        filterChainDefinitionMap.put("/lark_suite/info", "anon");
        filterChainDefinitionMap.put("/sso/callback/we_com", "anon");
        filterChainDefinitionMap.put("/sso/callback/ding_talk", "anon");
        filterChainDefinitionMap.put("/sso/callback/lark", "anon");
        filterChainDefinitionMap.put("/sso/callback/lark_suite", "anon");
        filterChainDefinitionMap.put("/setting/get/platform/param", "anon");
        filterChainDefinitionMap.put("/signout", "anon");
        filterChainDefinitionMap.put("/is-login", "anon");
        filterChainDefinitionMap.put("/get-key", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/images/**", "anon");
        filterChainDefinitionMap.put("/assets/**", "anon");
        filterChainDefinitionMap.put("/fonts/**", "anon");
        filterChainDefinitionMap.put("/display/info", "anon");
        filterChainDefinitionMap.put("/file/preview/**", "anon");
        filterChainDefinitionMap.put("/favicon.ico", "anon");
        filterChainDefinitionMap.put("/base-display/**", "anon");
        filterChainDefinitionMap.put("/jmeter/ping", "anon");
        filterChainDefinitionMap.put("/authsource/list/allenable", "anon");
        filterChainDefinitionMap.put("/sso/callback/**", "anon");
        filterChainDefinitionMap.put("/license/validate", "anon");
        //mock-server
        filterChainDefinitionMap.put("/mock-server/**", "anon");

        //功能用例富文本访问
        filterChainDefinitionMap.put("/attachment/download/file/**", "anon");
        //用例评审富文本访问
        filterChainDefinitionMap.put("/review/functional/case/download/file/**", "anon");
        //缺陷管理富文本访问
        filterChainDefinitionMap.put("/bug/attachment/preview/md/**", "anon");
        //计划报告富文本访问
        filterChainDefinitionMap.put("/test-plan/report/preview/md/**", "anon");
        //模板富文本框图片预览
        filterChainDefinitionMap.put("/organization/template/img/preview/**", "anon");
        filterChainDefinitionMap.put("/project/template/img/preview/**", "anon");

        filterChainDefinitionMap.put("/system/version/current", "anon");

        //用户通过邮箱邀请自行注册的接口
        filterChainDefinitionMap.put("/system/user/check-invite/**", "anon");
        filterChainDefinitionMap.put("/system/user/register-by-invite", "anon");

        // 下载测试资源
        filterChainDefinitionMap.put("/api/execute/resource/**", "anon");


        // for swagger
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");
        filterChainDefinitionMap.put("/swagger-ui/**", "anon");
        filterChainDefinitionMap.put("/v3/api-docs/**", "anon");

        filterChainDefinitionMap.put("/403", "anon");
        filterChainDefinitionMap.put("/anonymous/**", "anon");

        //分享相关接口
        filterChainDefinitionMap.put("/api/share/doc/view/**", "anon");

        filterChainDefinitionMap.put("/system/theme", "anon");
        filterChainDefinitionMap.put("/system/parameter/save/base-url/**", "anon");
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
        filterChainDefinitionMap.put("/templates/user_import_en.xlsx", "anon");
        filterChainDefinitionMap.put("/templates/user_import_cn.xlsx", "anon");

        //分享报告接口
        filterChainDefinitionMap.put("/api/report/case/share/**", "anon");
        filterChainDefinitionMap.put("/api/report/scenario/share/**", "anon");
        filterChainDefinitionMap.put("/api/report/share/get/**", "anon");
        // 测试计划报告分享接口
        filterChainDefinitionMap.put("/test-plan/report/share/detail/**", "anon");
        filterChainDefinitionMap.put("/test-plan/report/share/get/**", "anon");
        filterChainDefinitionMap.put("/test-plan/report/share/get-layout/**", "anon");
        // 默认语言
        filterChainDefinitionMap.put("/user/local/config/default-locale", "anon");
        // 定义-分享
        filterChainDefinitionMap.put("/api/doc/share/detail/**", "anon");
        filterChainDefinitionMap.put("/api/doc/share/get-detail/**", "anon");
        filterChainDefinitionMap.put("/api/doc/share/check/**", "anon");
        filterChainDefinitionMap.put("/api/doc/share/module/**", "anon");
        filterChainDefinitionMap.put("/api/doc/share/export/**", "anon");
        filterChainDefinitionMap.put("/api/doc/share/stop/**", "anon");
        filterChainDefinitionMap.put("/api/doc/share/download/file/**", "anon");
        filterChainDefinitionMap.put("/api/doc/share/plugin/script/**", "anon");

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
