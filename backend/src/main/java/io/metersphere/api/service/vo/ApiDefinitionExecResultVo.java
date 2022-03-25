package io.metersphere.api.service.vo;

import io.metersphere.dto.RequestResult;
import lombok.Data;

@Data
public class ApiDefinitionExecResultVo {
    private String id;

    private String name;

    private String resourceId;

    private String status;

    private String userId;

    private Long startTime;

    private Long endTime;

    private Long createTime;

    private String type;

    private String actuator;

    private String triggerMode;

    private String errorCode;

    private String versionId;

    private String projectId;

    private String integratedReportId;

    private long totalAssertions = 0;

    private long passAssertions = 0;

    private RequestResult requestResult;
}
