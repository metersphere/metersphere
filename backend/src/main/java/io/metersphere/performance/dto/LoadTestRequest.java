package io.metersphere.performance.dto;

import io.metersphere.controller.request.BaseQueryRequest;
import lombok.Data;

@Data
public class LoadTestRequest extends BaseQueryRequest {
    private String id;

    private String projectId;

    private String name;

    private String description;

    private Long createTime;

    private Long updateTime;

    private String status;

    private String testResourcePoolId;

    private String userId;

    private Integer num;

    private String createUser;

    private Integer scenarioVersion;

    private String scenarioId;

    private Long order;

    private String refId;

    private String versionId;

    private Boolean latest;

}
