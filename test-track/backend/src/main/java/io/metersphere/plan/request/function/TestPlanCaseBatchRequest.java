package io.metersphere.plan.request.function;

import io.metersphere.base.domain.TestPlanTestCase;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestPlanCaseBatchRequest extends TestPlanTestCase {
    private List<String> ids;
    private TestPlanFuncCaseConditions condition;
    private boolean modifyExecutor;
}
