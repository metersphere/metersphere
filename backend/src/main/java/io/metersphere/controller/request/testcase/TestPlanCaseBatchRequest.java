package io.metersphere.controller.request.testcase;

import io.metersphere.base.domain.TestCase;
import io.metersphere.base.domain.TestPlanTestCase;
import lombok.Data;

import java.util.List;

@Data
public class TestPlanCaseBatchRequest extends TestPlanTestCase {

    private List<String> ids;

}
