package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestCaseReviewFollow implements Serializable {
    private String reviewId;

    private String followId;

    private static final long serialVersionUID = 1L;
}