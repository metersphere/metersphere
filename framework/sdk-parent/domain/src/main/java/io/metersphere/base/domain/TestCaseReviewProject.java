package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestCaseReviewProject implements Serializable {
    private String reviewId;

    private String projectId;

    private static final long serialVersionUID = 1L;
}