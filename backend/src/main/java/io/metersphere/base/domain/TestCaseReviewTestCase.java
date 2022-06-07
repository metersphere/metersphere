package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseReviewTestCase implements Serializable {
    private String id;

    private String reviewId;

    private String caseId;

    private String result;

    private String reviewer;

    private Long createTime;

    private Long updateTime;

    private String createUser;

    private Long order;

    private String status;

    private Boolean isDel;

    private static final long serialVersionUID = 1L;
}