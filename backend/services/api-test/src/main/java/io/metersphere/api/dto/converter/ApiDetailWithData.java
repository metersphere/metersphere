package io.metersphere.api.dto.converter;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ApiDetailWithData {
    @Schema(description = "相同的数据的key")
    List<String> sameList = new ArrayList<>();
    @Schema(description = "不同的数据的key")
    List<String> differenceList = new ArrayList<>();
    @Schema(description = "数据库中存在的数据")
    Map<String, ApiDefinitionImportDetail> apiDateMap = new HashMap<>();
    @Schema(description = "导入的数据")
    Map<String, ApiDefinitionImportDetail> importDataMap = new HashMap<>();

}
