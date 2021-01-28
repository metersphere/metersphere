package io.metersphere.track.request.testplan;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestplanRunRequest {
    private String testPlanID;
    private String projectID;
    private String userId;
    private String triggerMode;
}

