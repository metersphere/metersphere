package io.metersphere.log.vo.definition;

import java.util.LinkedHashMap;
import java.util.Map;

public class DefinitionReference {
    public static Map<String, String> definitionColumns = new LinkedHashMap<>();

    static {
        definitionColumns.put("name", "接口名称");
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
        definitionColumns.put("ms-dff-col", "request");
    }
}