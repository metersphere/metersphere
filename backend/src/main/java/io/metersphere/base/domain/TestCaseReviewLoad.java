package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseReviewLoad implements Serializable {
    private String id;

    private String testCaseReviewId;

    private String loadCaseId;

    private String status;

    private Long createTime;

    private Long updateTime;

    private String loadReportId;

    private static final long serialVersionUID = 1L;
}