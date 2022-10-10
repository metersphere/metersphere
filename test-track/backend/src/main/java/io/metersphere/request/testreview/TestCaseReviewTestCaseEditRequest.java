package io.metersphere.request.testreview;

import io.metersphere.base.domain.TestCaseReviewTestCase;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestCaseReviewTestCaseEditRequest extends TestCaseReviewTestCase {
    private String comment;
}
