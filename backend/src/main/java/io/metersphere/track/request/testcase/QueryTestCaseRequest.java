package io.metersphere.track.request.testcase;

import io.metersphere.base.domain.TestCase;
import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QueryTestCaseRequest extends BaseQueryRequest {

    private String name;

    private List<String> testCaseIds;

    private String planId;

    private String userId;

    private String reviewId;
}
