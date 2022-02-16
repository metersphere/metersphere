package io.metersphere.track.request.testcase;

import io.metersphere.controller.request.BaseQueryRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IssuesRequest extends BaseQueryRequest {
    private String title;
    private String content;
    private String projectId;
    private String testCaseId;
    private List<String> tapdUsers;
    private String userId;
    /**
     * 关联类型
     * 如果类型是 FUNCTIONAL 表示是功能用例查询相关缺陷，此时需要把该功能用例对应的测试计划中的用例关联的缺陷一并查出
     * 如果是 PLAN_FUNCTIONAL 则只查询该测试计划用例所关联的缺陷
     */
    private String refType;
    /**
     * zentao bug 处理人
     */
    private String zentaoUser;
    /**
     * zentao bug 影响版本
     */
    private List<String> zentaoBuilds;

    /**
     * issues id
     */
    private String id;
    private String caseId;
    private String resourceId;
    /**
     * 查询用例下的缺陷的用例id或者测试计划的用例id
     */
    private String caseResourceId;
    private String platform;
    private String customFields;
    private List<String> testCaseIds;
    private List<String> notInIds;

    private String requestType;
    private String status;
    private String defaultCustomFields;
    private Boolean isPlanEdit = false;
    private String planId;
}
