package io.metersphere.performance.dto;

import io.metersphere.base.domain.LoadTestWithBLOBs;
import io.metersphere.controller.request.OrderRequest;
import lombok.Data;

import java.util.List;

@Data
public class LoadTestBatchRequest extends LoadTestWithBLOBs {
    private List<String> ids;
    private String name;
    private List<OrderRequest> orders;
    private String projectId;
    private String moduleId;
    private String protocol;

    private LoadTestRequest condition;
}
