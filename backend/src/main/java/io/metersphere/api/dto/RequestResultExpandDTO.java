package io.metersphere.api.dto;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.base.domain.ApiScenarioReportResultWithBLOBs;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.ResponseResult;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RequestResultExpandDTO extends RequestResult {
    private String status;
    private String uiImg;
    private String uiScreenshots;
    private String combinationImg;
    private String reportId;
    private long time;
    private Map<String, String> attachInfoMap;

    public RequestResultExpandDTO() {
    }

    public RequestResultExpandDTO(String name, String status) {
        this.setName(name);
        this.setStatus(status);
    }

    public RequestResultExpandDTO(ApiScenarioReportResultWithBLOBs requestResult) {
        if (requestResult.getBaseInfo() != null) {
            try {
                ApiScenarioReportBaseInfoDTO dto = JSONObject.parseObject(requestResult.getBaseInfo(), ApiScenarioReportBaseInfoDTO.class);
                this.setName(dto.getReqName());
                this.setSuccess(dto.isReqSuccess());
                this.setError(dto.getReqError());
                this.setStartTime(dto.getReqStartTime());
                this.setTime(dto.getRspTime());
                this.setEndTime(dto.getRspTime() - dto.getReqStartTime());
                this.setUiImg(dto.getUiImg());
                this.setUiScreenshots(dto.getUiScreenshots());
                this.setCombinationImg(dto.getCombinationImg());
                this.setReportId(dto.getReportId());
                this.setStatus(requestResult.getStatus());
                ResponseResult responseResult = this.getResponseResult();
                responseResult.setResponseCode(dto.getRspCode());
                responseResult.setResponseTime(dto.getRspTime());
                this.setResponseResult(responseResult);
            } catch (Exception e) {
                LogUtil.error("Parse report base-info error :" + e.getMessage());
            }
        }
    }
}
