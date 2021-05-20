package io.metersphere.log.vo.system;

import java.util.LinkedHashMap;
import java.util.Map;

public class SystemReference {
    public static Map<String, String> userColumns = new LinkedHashMap<>();
    public static Map<String, String> organizationColumns = new LinkedHashMap<>();
    public static Map<String, String> poolColumns = new LinkedHashMap<>();
    public static Map<String, String> authConfigColumns = new LinkedHashMap<>();
    public static Map<String, String> quotaColumns = new LinkedHashMap<>();
    public static Map<String, String> serverColumns = new LinkedHashMap<>();
    public static Map<String, String> messageColumns = new LinkedHashMap<>();
    public static Map<String, String> fieldColumns = new LinkedHashMap<>();
    public static Map<String, String> caseFieldColumns = new LinkedHashMap<>();
    public static Map<String, String> issueFieldColumns = new LinkedHashMap<>();
    public static Map<String, String> projectColumns = new LinkedHashMap<>();
    public static Map<String, String> jarColumns = new LinkedHashMap<>();
    public static Map<String, String> environmentColumns = new LinkedHashMap<>();

    static {

        userColumns.clear();
        organizationColumns.clear();
        poolColumns.clear();
        authConfigColumns.clear();
        quotaColumns.clear();
        serverColumns.clear();
        messageColumns.clear();
        fieldColumns.clear();
        caseFieldColumns.clear();
        issueFieldColumns.clear();
        projectColumns.clear();
        jarColumns.clear();
        environmentColumns.clear();

        userColumns.put("name", "用户名称");
        userColumns.put("createUser", "创建人");
        userColumns.put("email", "邮箱");
        userColumns.put("status", "状态");
        userColumns.put("phone", "电话");
        userColumns.put("workspace", "工作空间");
        userColumns.put("organization", "组织");

        organizationColumns.put("name", "名称");
        organizationColumns.put("description", "描述");

        poolColumns.put("name", "名称");
        poolColumns.put("type", "类型");
        poolColumns.put("image", "镜像");
        poolColumns.put("heap", "JMeter HEAP");
        poolColumns.put("gcAlgo", "JMeter GC_ALGO");
        poolColumns.put("description", "描述");

        authConfigColumns.put("name", "名称");
        authConfigColumns.put("type", "类型");
        authConfigColumns.put("status", "状态");
        authConfigColumns.put("configuration", "配置");
        authConfigColumns.put("description", "描述");

        quotaColumns.put("api", "接口测试数量");
        quotaColumns.put("performance", "性能测试数量");
        quotaColumns.put("maxThreads", "最大并发数");
        quotaColumns.put("duration", "压测时长（秒）");

        serverColumns.put("platform", "平台");
        serverColumns.put("configuration", "服务配置");

        messageColumns.put("type", "通知类型");
        messageColumns.put("event", "事件");
        messageColumns.put("taskType", "任务类型");
        messageColumns.put("webhook", "勾子");
        messageColumns.put("identification", "鉴别");
        messageColumns.put("template", "模版");

        fieldColumns.put("name", "用户名称");
        fieldColumns.put("scene", "使用场景");
        fieldColumns.put("type", "属性类型");
        fieldColumns.put("system", "系统字段");
        fieldColumns.put("remark", "字段备注");

        caseFieldColumns.put("name", "用户名称");
        caseFieldColumns.put("caseName", "用例名称");
        caseFieldColumns.put("type", "用例类型");
        caseFieldColumns.put("steps", "步骤");
        caseFieldColumns.put("stepDescription", "步骤描述");
        caseFieldColumns.put("description", "描述");

        issueFieldColumns.put("name", "名称");
        issueFieldColumns.put("platform", "缺陷平台");
        issueFieldColumns.put("title", "标题");
        issueFieldColumns.put("content", "缺陷内容");
        issueFieldColumns.put("description", "描述");

        projectColumns.put("name", "名称");
        projectColumns.put("description", "描述");

        jarColumns.put("name", "名称");
        jarColumns.put("fileName", "文件名称");
        jarColumns.put("description", "描述");


        environmentColumns.put("name", "环境名称");
        environmentColumns.put("variables", "变量");
        environmentColumns.put("config", "配置");
        // 深度对比字段
        environmentColumns.put("ms-dff-col", "config,variables");

    }
}