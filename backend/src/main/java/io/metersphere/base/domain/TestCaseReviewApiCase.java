package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseReviewApiCase implements Serializable {
    private String id;

    private String testCaseReviewId;

    private String apiCaseId;

    private String status;

    private String environmentId;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}