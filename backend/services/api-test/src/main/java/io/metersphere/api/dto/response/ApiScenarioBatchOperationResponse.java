package io.metersphere.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ApiScenarioBatchOperationResponse {
    @Schema(description = "成功的数据")
    private long success;
    @Schema(description = "失败的数据量")
    private long error;
    @Schema(description = "成功数据")
    List<OperationDataInfo> successData = new ArrayList<>();
    @Schema(description = "失败数据")
    Map<String, List<OperationDataInfo>> errorInfo = new HashMap<>();

    public void addSuccessData(String id, Long num, String name) {
        OperationDataInfo operationData = new OperationDataInfo(id, num, name);
        successData.add(operationData);
        this.success = successData.size();
    }

    public void addErrorData(String errorMessage, String id, Long num, String name) {
        OperationDataInfo errorData = new OperationDataInfo(id, num, name);
        if (!errorInfo.containsKey(errorMessage)) {
            this.errorInfo.put(errorMessage, new ArrayList<>());
        }
        this.errorInfo.get(errorMessage).add(errorData);
        this.error++;
    }

    public void merge(ApiScenarioBatchOperationResponse response) {
        this.successData.addAll(response.getSuccessData());
        this.success = successData.size();
        response.getErrorInfo().forEach((errorMessage, errorDataList) -> {
            if (!this.errorInfo.containsKey(errorMessage)) {
                this.errorInfo.put(errorMessage, new ArrayList<>());
            }
            this.errorInfo.get(errorMessage).addAll(errorDataList);
            this.error += errorDataList.size();
        });
    }
}
