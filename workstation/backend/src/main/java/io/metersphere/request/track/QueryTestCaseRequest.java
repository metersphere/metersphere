package io.metersphere.request.track;

import io.metersphere.request.BaseQueryRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QueryTestCaseRequest extends BaseQueryRequest {

    private String id;

    private String name;

    private String relationshipType;

    private List<String> testCaseIds;

    // 测试计划是否允许重复
    private boolean repeatCase;

    private String planId;

    private String issuesId;

    private String userId;

    private String reviewId;

    private boolean isSelectThisWeedData = false;
    private boolean isSelectThisWeedRelevanceData = false;

    private String caseCoverage;

    private String nodeId;

    private String statusIsNot;

    private long createTime = 0;
    private long relevanceCreateTime = 0;
    private List<String> testCaseContainIds;


    // 接口定义
    private String protocol;
    private String apiCaseCoverage;

    // 补充场景条件
    private String excludeId;
    private String moduleId;
    private boolean recent = false;
    private String executeStatus;
    private boolean notInTestPlan;
    //操作人
    private String operator;
    //操作时间
    private Long operationTime;
    private boolean casePublic;
    private long scheduleCreateTime;
    private String stepTotal;
    private Boolean toBeUpdated;
    private String apiCoverage;
    private String scenarioCoverage;
}
