package io.metersphere.log.vo.api;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefinitionReference {
    public static Map<String, String> definitionColumns = new LinkedHashMap<>();
    public static Map<String, String> caseColumns = new LinkedHashMap<>();

    static {
        definitionColumns.clear();
        caseColumns.clear();
        definitionColumns.put("name", "接口名称");
        definitionColumns.put("createUser", "创建人");
        definitionColumns.put("method", "请求类型");
        definitionColumns.put("modulePath", "模块");
        definitionColumns.put("status", "接口状态");
        definitionColumns.put("protocol", "协议");
        definitionColumns.put("userId", "负责人");
        definitionColumns.put("path", "路径");
        definitionColumns.put("tags", "标签");
        definitionColumns.put("request", "请求参数");
        definitionColumns.put("response", "返回参数");
        definitionColumns.put("description", "描述");
        // 需要深度对比的字段，可以支持多个req1,req2
        definitionColumns.put("ms-dff-col", "request,response,tags");

        // 用例列数据
        caseColumns.put("name", "用例名称");
        caseColumns.put("priority", "用例级别");
        caseColumns.put("createUserId", "创建人");
        caseColumns.put("updateUserId", "编辑人");
        caseColumns.put("tags", "标签");
        caseColumns.put("description", "描述");
        caseColumns.put("request", "请求参数");
        // 深度对比字段
        caseColumns.put("ms-dff-col", "request,tags");

    }
}