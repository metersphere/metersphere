package io.metersphere.log.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OperatingLogRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String workspaceId;

    private String projectId;

    private String operUser;

    private String sourceId;

    private String operType;

    private Long startTime;

    private Long endTime;

    private String operModule;

    private String operTitle;

    private List<Long> times;

    private List<String> projectIds;

}