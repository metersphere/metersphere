package io.metersphere.track.request.testcase;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QueryNodeRequest {

    private String testPlanId;
    private String projectId;
    private String reviewId;

    private List<String> moduleIds;
}
