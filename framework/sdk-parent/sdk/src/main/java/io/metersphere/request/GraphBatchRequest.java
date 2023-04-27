package io.metersphere.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GraphBatchRequest {
    private List<String> ids;
    private String name;
    private List<OrderRequest> orders;
    private String projectId;
    private QueryTestCaseRequest condition;
}
