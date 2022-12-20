package io.metersphere.dto;

import io.metersphere.base.domain.TestCaseReview;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestCaseReviewWithMaintainer extends TestCaseReview {

    private String maintainer;
}
