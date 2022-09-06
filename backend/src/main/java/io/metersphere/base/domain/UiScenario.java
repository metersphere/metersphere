package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class UiScenario implements Serializable {
    private String id;

    private String projectId;

    private String tags;

    private String userId;

    private String moduleId;

    private String modulePath;

    private String name;

    private String scenarioType;

    private String level;

    private String status;

    private String principal;

    private Integer stepTotal;

    private String schedule;

    private Long createTime;

    private Long updateTime;

    private String passRate;

    private String lastResult;

    private String reportId;

    private Integer num;

    private String originalState;

    private String customNum;

    private String createUser;

    private Integer version;

    private Long deleteTime;

    private String deleteUserId;

    private Integer executeTimes;

    private Long order;

    private String environmentType;

    private String environmentGroupId;

    private String versionId;

    private String refId;

    private Boolean latest;

    private static final long serialVersionUID = 1L;
}
