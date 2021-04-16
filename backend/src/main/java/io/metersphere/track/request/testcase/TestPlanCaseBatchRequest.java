package io.metersphere.track.request.testcase;

import io.metersphere.base.domain.TestPlanTestCase;
import io.metersphere.track.request.testplancase.TestPlanFuncCaseConditions;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestPlanCaseBatchRequest extends TestPlanTestCase {
    private List<String> ids;
    private TestPlanFuncCaseConditions condition;
}
