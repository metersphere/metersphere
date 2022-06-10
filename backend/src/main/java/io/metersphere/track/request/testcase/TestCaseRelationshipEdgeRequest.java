package io.metersphere.track.request.testcase;

import io.metersphere.controller.request.RelationshipEdgeRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestCaseRelationshipEdgeRequest extends RelationshipEdgeRequest {
    private List<String> ids;
    private QueryTestCaseRequest condition;
}
