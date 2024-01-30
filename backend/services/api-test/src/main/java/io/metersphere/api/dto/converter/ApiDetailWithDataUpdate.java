package io.metersphere.api.dto.converter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ApiDetailWithDataUpdate {
    @Schema(description = "需要更新模块的数据")
    List<ApiDefinitionImportDetail> updateModuleData = new ArrayList<>();
    @Schema(description = "需要更新接口的数据")
    List<ApiDefinitionImportDetail> updateRequestData = new ArrayList<>();
    @Schema(description = "需要新增的接口数据")
    List<ApiDefinitionImportDetail> addModuleData = new ArrayList<>();
    @Schema(description = "需要新增的日志数据")
    Map<String, ApiDefinitionImportDetail> logData;

}
