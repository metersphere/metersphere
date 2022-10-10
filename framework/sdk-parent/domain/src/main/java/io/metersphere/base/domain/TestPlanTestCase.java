package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestPlanTestCase implements Serializable {
    private String id;

    private String planId;

    private String caseId;

    private String reportId;

    private String executor;

    private String remark;

    private Long createTime;

    private Long updateTime;

    private String createUser;

    private Integer issuesCount;

    private Long order;

    private String status;

    private Boolean isDel;

    private static final long serialVersionUID = 1L;
}