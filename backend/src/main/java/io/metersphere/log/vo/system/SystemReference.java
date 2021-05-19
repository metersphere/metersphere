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

    static {

        userColumns.clear();
        organizationColumns.clear();
        poolColumns.clear();
        authConfigColumns.clear();
        quotaColumns.clear();
        serverColumns.clear();
        messageColumns.clear();

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

    }
}