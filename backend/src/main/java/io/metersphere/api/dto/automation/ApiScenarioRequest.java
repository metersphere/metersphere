package io.metersphere.api.dto.automation;

import io.metersphere.controller.request.BaseQueryRequest;
import io.metersphere.controller.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ApiScenarioRequest extends BaseQueryRequest {
    private String id;
    private String excludeId;
    private String moduleId;
    private String name;
    private String userId;
    private String planId;
    private boolean recent = false;
    private boolean isSelectThisWeedData;
    private long createTime = 0;
    private String executeStatus;
    private boolean notInTestPlan;
    private String reviewId;

    //操作人
    private String operator;
    //操作时间
    private Long operationTime;
}
