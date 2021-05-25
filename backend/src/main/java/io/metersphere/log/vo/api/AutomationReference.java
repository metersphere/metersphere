package io.metersphere.log.vo.api;

import java.util.LinkedHashMap;
import java.util.Map;

public class AutomationReference {
    public static Map<String, String> automationColumns = new LinkedHashMap<>();

    static {
        automationColumns.clear();
        automationColumns.put("name", "场景名称");
        automationColumns.put("createUser", "创建人");
        automationColumns.put("method", "请求类型");
        automationColumns.put("modulePath", "模块");
        automationColumns.put("level", "场景级别");
        automationColumns.put("status", "场景状态");
        automationColumns.put("principal", "责任人");
        automationColumns.put("stepTotal", "步骤数");
        automationColumns.put("passRate", "通过率");
        automationColumns.put("lastResult", "最后执行结果");
        automationColumns.put("tags", "标签");
        automationColumns.put("scenarioDefinition", "场景步骤");
        automationColumns.put("description", "描述");
        // 需要深度对比的字段，可以支持多个req1,req2
        automationColumns.put("ms-dff-col", "scenarioDefinition,tags");
    }
}