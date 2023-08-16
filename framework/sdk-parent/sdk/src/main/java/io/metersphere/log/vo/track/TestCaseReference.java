package io.metersphere.log.vo.track;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestCaseReference {
    public static Map<String, String> testCaseColumns = new LinkedHashMap<>();

    static {
        testCaseColumns.clear();
        testCaseColumns.put("name", "test_case_name");
        testCaseColumns.put("createUser", "create_user");
        testCaseColumns.put("nodePath", "module");
        testCaseColumns.put("type", "test_case_type");
        testCaseColumns.put("maintainer", "maintainer");
        testCaseColumns.put("priority", "test_case_priority");
        testCaseColumns.put("method", "method");
        testCaseColumns.put("prerequisite", "preconditions");
        testCaseColumns.put("remark", "remark");
        testCaseColumns.put("customNum", "ID");
        testCaseColumns.put("steps", "case_step");
        testCaseColumns.put("other_test_name", "other_test_name");
        testCaseColumns.put("review_status", "review_status");
        testCaseColumns.put("tags", "tag");
        testCaseColumns.put("demand_name", "demand_name");
        testCaseColumns.put("follow_people", "follow_people");
        testCaseColumns.put("status", "test_case_status");
        testCaseColumns.put("stepDescription", "test_case_step_desc");
        testCaseColumns.put("expectedResult", "test_case_step_result");
        testCaseColumns.put("demandName", "related_requirements");
        testCaseColumns.put("followPeople", "follow_people");
        testCaseColumns.put("comment", "comment");
        testCaseColumns.put("ms-dff-col", "tags,steps");

    }
}