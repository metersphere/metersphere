package io.metersphere.api.dto.definition.importdto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ApiDeatlWithDataUpdate {
    @Schema(description = "需要更新模块的数据")
    List<ApiDefinitionImportDTO> updateModuleData = new ArrayList<>();
    @Schema(description = "需要更新接口的数据")
    List<ApiDefinitionImportDTO> updateRequestData = new ArrayList<>();
    @Schema(description = "需要新增的接口数据")
    List<ApiDefinitionImportDTO> addModuleData = new ArrayList<>();
    @Schema(description = "需要新增的日志数据")
    Map<String, ApiDefinitionImportDTO> logData;

}
