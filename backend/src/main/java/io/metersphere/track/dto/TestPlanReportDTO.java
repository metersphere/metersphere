package io.metersphere.track.dto;

import io.metersphere.base.domain.Issues;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author song.tianyang
 * @Date 2021/1/8 2:39 下午
 * @Description
 */
@Getter
@Setter
public class TestPlanReportDTO {
    private String id;
    private String testPlanId;
    private Boolean isNew;
    private String name;
    private String testPlanName;
    private String creator;
    private long createTime;
    private String triggerMode;
    private String status;
    private String reportComponents;

    private TestCaseReportAdvanceStatusResultDTO executeResult;
    private List<TestCaseReportModuleResultDTO> moduleExecuteResult;
    private FailureTestCasesAdvanceDTO failureTestCases;
    private List<Issues> Issues;
    private List<String> executors;
    private String principal;
    private String principalName;
    private Long startTime;
    private Long endTime;
    private String projectName;

    private Long runTime;
    private Double passRate;
}
