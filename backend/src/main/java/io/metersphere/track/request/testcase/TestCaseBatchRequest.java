package io.metersphere.track.request.testcase;

import io.metersphere.base.domain.TestCaseWithBLOBs;
import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestCaseBatchRequest extends TestCaseWithBLOBs {
    private List<String> ids;
    private List<OrderRequest> orders;
    private String projectId;
}
