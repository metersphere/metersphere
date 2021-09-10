package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestPlanTestCase implements Serializable {
    private String id;

    private String planId;

    private String caseId;

    private String reportId;

    private String executor;

    private String status;

    private String remark;

    private Long createTime;

    private Long updateTime;

    private String createUser;

    private Integer issuesCount;

    private Long order;

    private static final long serialVersionUID = 1L;
}
