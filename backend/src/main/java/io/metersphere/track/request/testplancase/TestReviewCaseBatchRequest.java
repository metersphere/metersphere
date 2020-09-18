package io.metersphere.track.request.testplancase;

import io.metersphere.base.domain.TestCaseReviewTestCase;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestReviewCaseBatchRequest extends TestCaseReviewTestCase {
    private List<String> ids;
}
