package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestCase implements Serializable {
    private String id;

    private String nodeId;

    private String testId;

    @Deprecated
    private String nodePath;

    private String projectId;

    private String name;

    @Deprecated
    private String type;

    private String maintainer;

    private String priority;

    @Deprecated
    private String method;

    private Long createTime;

    private Long updateTime;

    private Integer sort;

    private Integer num;

    private String reviewStatus;

    private String tags;

    private String demandId;

    private String demandName;

    private String status;

    private String stepModel;

    private String customNum;

    private String createUser;

    private String originalStatus;

    private Long deleteTime;

    private String deleteUserId;

    private Long order;

    private Boolean casePublic;

    private String versionId;

    private String refId;

    private Boolean latest;

    private String lastExecuteResult;

    private static final long serialVersionUID = 1L;
}
