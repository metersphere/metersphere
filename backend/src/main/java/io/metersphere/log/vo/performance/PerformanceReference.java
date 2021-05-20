package io.metersphere.log.vo.performance;

import java.util.LinkedHashMap;
import java.util.Map;

public class PerformanceReference {
    public static Map<String, String> performanceColumns = new LinkedHashMap<>();
    public static Map<String, String> reportColumns = new LinkedHashMap<>();

    static {
        performanceColumns.clear();
        reportColumns.clear();
        performanceColumns.put("name", "用例名称");
        performanceColumns.put("status", "状态");
        performanceColumns.put("loadConfiguration", "压力配置");
        performanceColumns.put("advancedConfiguration", "高级配置");
        performanceColumns.put("description", "描述");
        performanceColumns.put("ms-dff-col", "loadConfiguration,advancedConfiguration");

        reportColumns.put("name","报告名称");
    }
}