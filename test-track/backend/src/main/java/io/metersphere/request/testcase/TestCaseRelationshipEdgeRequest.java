package io.metersphere.request.testcase;

import io.metersphere.request.RelationshipEdgeRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestCaseRelationshipEdgeRequest extends RelationshipEdgeRequest {
    private List<String> ids;
    private QueryTestCaseRequest condition;
}
