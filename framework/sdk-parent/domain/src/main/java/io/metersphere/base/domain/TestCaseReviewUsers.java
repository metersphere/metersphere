package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class TestCaseReviewUsers implements Serializable {
    private String reviewId;

    private String userId;

    private static final long serialVersionUID = 1L;
}