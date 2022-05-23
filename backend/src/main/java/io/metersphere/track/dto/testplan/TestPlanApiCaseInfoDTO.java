package io.metersphere.track.dto.testplan;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestPlanApiCaseInfoDTO {
    private String id;
    private String apiCaseId;
    private String environmentId;
    private String projectId;
}
