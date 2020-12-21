package io.metersphere.track.request.testcase;

import io.metersphere.base.domain.TestPlanTestCase;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestPlanApiCaseBatchRequest extends TestPlanTestCase {
    private List<String> ids;
}
