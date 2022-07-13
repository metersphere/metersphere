package io.metersphere.track.request.testplancase;

import io.metersphere.base.domain.TestPlanTestCaseWithBLOBs;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestPlanFuncCaseEditRequest extends TestPlanTestCaseWithBLOBs {
    private String comment;
}
