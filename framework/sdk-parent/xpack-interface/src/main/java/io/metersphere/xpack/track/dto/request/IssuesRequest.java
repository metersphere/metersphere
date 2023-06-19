package io.metersphere.xpack.track.dto.request;

import io.metersphere.request.BaseQueryRequest;
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

    /**
     * 是否根据自定义字段进行排序
     */
    private Boolean isCustomSorted = false;
    /**
     * 自定义字段ID
     */
    private String customFieldId;

    /**
     * 缺陷导出勾选ID
     */
    private List<String> exportIds;

    /**
     * 本周测试计划关联缺陷
     */
    private Boolean thisWeekUnClosedTestPlanIssue = false;
    /**
     * 测试计划遗留的缺陷
     */
    private Boolean unClosedTestPlanIssue = false;
    /**
     * 测试计划关联所有缺陷
     */
    private Boolean allTestPlanIssue = false;
    /**
     * 过滤缺陷ID
     */
    private List<String> filterIds;
}
