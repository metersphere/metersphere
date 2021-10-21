package io.metersphere.track.request.testplan;

import io.metersphere.base.domain.TestPlanWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddTestPlanRequest extends TestPlanWithBLOBs {
    private List<String> projectIds;
    private List<String> principals;
    private List<String> follows;
}
