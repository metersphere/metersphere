package io.metersphere.base.domain;

import java.io.Serializable;

import lombok.Data;

@Data
public class TestCaseReviewFollow implements Serializable {
    private String reviewId;

    private String followId;

    private static final long serialVersionUID = 1L;
}