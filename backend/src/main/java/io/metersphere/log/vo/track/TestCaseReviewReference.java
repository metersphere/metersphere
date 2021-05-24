package io.metersphere.log.vo.track;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestCaseReviewReference {
    public static Map<String, String> testCaseReviewColumns = new LinkedHashMap<>();
    public static Map<String, String> commentReviewColumns = new LinkedHashMap<>();

    static {
        testCaseReviewColumns.clear();
        commentReviewColumns.clear();
        testCaseReviewColumns.put("name", "用例名称");
        testCaseReviewColumns.put("createUser", "创建人");
        testCaseReviewColumns.put("status", "状态");
        testCaseReviewColumns.put("tags", "标签");
        testCaseReviewColumns.put("endTime", "截止时间");
        testCaseReviewColumns.put("description", "描述");
        testCaseReviewColumns.put("ms-dff-col", "tags");

        commentReviewColumns.put("author","评论人");
        commentReviewColumns.put("description","评论内容");
    }
}