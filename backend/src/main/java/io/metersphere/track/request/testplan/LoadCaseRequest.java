package io.metersphere.track.request.testplan;

import io.metersphere.base.domain.TestPlanLoadCase;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoadCaseRequest extends TestPlanLoadCase {
    private String projectId;
    private List<String> caseIds;
}
