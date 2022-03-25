package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportDTO {

    private String id;
    private String testId;
    private String name;
    private String description;
    private Long createTime;
    private Long updateTime;
    private String status;
    private String content;
    private String testName;
    private String projectId;
    private String projectName;
    private String userName;
    private String triggerMode;
    private String maxUsers;
    private String avgResponseTime;
    private String tps;
    private long testStartTime;
    private long testEndTime;
    private long testDuration;
}
