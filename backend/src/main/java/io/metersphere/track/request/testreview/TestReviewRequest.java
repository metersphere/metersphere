package io.metersphere.track.request.testreview;

import io.metersphere.base.domain.TestCaseReviewLoad;
import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
@Getter
@Setter
public class TestReviewRequest extends TestCaseReviewLoad {
    private String projectId;
    private List<String> caseIds;
    private String name;
    private String status;
    private Map<String, List<String>> filters;
    private List<OrderRequest> orders;

}
