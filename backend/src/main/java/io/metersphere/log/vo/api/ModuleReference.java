package io.metersphere.log.vo.api;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModuleReference {
    public static Map<String, String> moduleColumns = new LinkedHashMap<>();

    static {
        moduleColumns.clear();
        // 模块列数据
        moduleColumns.put("name", "模块名称");
        moduleColumns.put("createUser", "创建人");
        moduleColumns.put("protocol", "协议");
        moduleColumns.put("level", "模块级别");

    }
}
