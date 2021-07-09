package io.metersphere.log.vo.track;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestCaseReference {
    public static Map<String, String> testCaseColumns = new LinkedHashMap<>();

    static {
        testCaseColumns.clear();
        testCaseColumns.put("name", "用例名称");
        testCaseColumns.put("createUser", "创建人");
        testCaseColumns.put("nodePath", "用例模块");
        testCaseColumns.put("type", "用例类型");
        testCaseColumns.put("maintainer", "责任人");
        testCaseColumns.put("priority", "用例等级");
        testCaseColumns.put("method", "请求类型");
        testCaseColumns.put("prerequisite", "前置条件");
        testCaseColumns.put("remark", "备注");
        testCaseColumns.put("customNum", "ID");
        testCaseColumns.put("steps", "用例步骤");
        testCaseColumns.put("other_test_name", "其他名称");
        testCaseColumns.put("review_status", "评审状态");
        testCaseColumns.put("tags", "标签");
        testCaseColumns.put("demand_name", "需求名称");
        testCaseColumns.put("follow_people", "关注人");
        testCaseColumns.put("status", "用例状态");
        testCaseColumns.put("stepDescription", "步骤描述");
        testCaseColumns.put("expectedResult", "预期结果");
        testCaseColumns.put("demandName", "关联需求");
        testCaseColumns.put("comment", "评论");
        testCaseColumns.put("ms-dff-col", "tags,steps");

    }
}