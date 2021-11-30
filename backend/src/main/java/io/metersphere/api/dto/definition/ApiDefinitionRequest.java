package io.metersphere.api.dto.definition;

import io.metersphere.controller.request.BaseQueryRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiDefinitionRequest extends BaseQueryRequest {

    private String id;
    private String excludeId;
    private String moduleId;
    private String protocol;
    private String name;
    private String userId;
    private String planId;
    private boolean recent = false;
    private boolean isSelectThisWeedData = false;
    private long createTime = 0;
    private String status;
    private String apiCaseCoverage;
    private String reviewId;
    private String refId;
    private String versionId;

    // 测试计划是否允许重复
    private boolean repeatCase;
}
