package io.metersphere.log.vo.track;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestPlanReference {

    public static Map<String, String> testPlanColumns = new LinkedHashMap<>();
    public static Map<String, String> reportColumns = new LinkedHashMap<>();
    public static Map<String, String> issuesColumns = new LinkedHashMap<>();

    static {
        testPlanColumns.clear();
        reportColumns.clear();
        issuesColumns.clear();
        testPlanColumns.put("name", "用例名称");
        testPlanColumns.put("creator", "创建人");
        testPlanColumns.put("principal", "责任人");
        testPlanColumns.put("status", "状态");
        testPlanColumns.put("stage", "测试阶段");
        testPlanColumns.put("tags", "标签");
        testPlanColumns.put("plannedStartTime", "计划开始时间");
        testPlanColumns.put("plannedEndTime", "计划结束时间");
        testPlanColumns.put("description", "描述");
        testPlanColumns.put("ms-dff-col", "tags");

        reportColumns.put("name", "名称");
        reportColumns.put("startTime", "开始时间");
        reportColumns.put("endTime", "结束时间");

        // 缺陷管理
        issuesColumns.put("title","缺陷标题");
        issuesColumns.put("status","缺陷状态");
        issuesColumns.put("platform","平台");
        issuesColumns.put("creator","创建人");
        issuesColumns.put("reporter","处理人");
        issuesColumns.put("customFields","自定义字段");
        // 深度处理字段
        issuesColumns.put("ms-dff-col", "customFields");

    }
}