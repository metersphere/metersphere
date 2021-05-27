package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class LoadTestReport implements Serializable {
    private String id;

    private String testId;

    private String name;

    private Long createTime;

    private Long updateTime;

    private String status;

    private String userId;

    private String triggerMode;

    private String fileId;

    private String maxUsers;

    private String avgResponseTime;

    private String tps;

    private String projectId;

    private String testName;

    private Long testStartTime;

    private Long testEndTime;

    private Long testDuration;

    private String testResourcePoolId;

    private static final long serialVersionUID = 1L;
}