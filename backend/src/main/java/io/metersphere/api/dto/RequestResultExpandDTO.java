package io.metersphere.api.dto;

import io.metersphere.base.domain.ApiScenarioReportResult;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResponseResult;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RequestResultExpandDTO extends RequestResult {
    private String status;
    private Map<String, String> attachInfoMap;

    public RequestResultExpandDTO() {
    }

    public RequestResultExpandDTO(ApiScenarioReportResult requestResult) {
        this.setName(requestResult.getReqName());
        this.setSuccess(requestResult.getReqSuccess());
        this.setError(requestResult.getReqError());
        this.setStartTime(requestResult.getReqStartTime());
        ResponseResult responseResult = this.getResponseResult();
        responseResult.setResponseCode(requestResult.getRspCode());
        responseResult.setResponseTime(requestResult.getRspTime());
        this.setResponseResult(responseResult);
    }
}
