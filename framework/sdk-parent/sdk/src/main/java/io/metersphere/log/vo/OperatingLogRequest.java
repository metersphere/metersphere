package io.metersphere.log.vo;

import io.metersphere.log.constants.OperatorLevel;
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

    private List<String> operModule;

    private String operTitle;

    private List<Long> times;

    private List<String> projectIds;

    private List<String> workspaceIds;

    private List<String> modules;

    //要查找的日志类型
    private String logType;

    private String level = OperatorLevel.PROJECT;
    private List<String> levelModules;

}