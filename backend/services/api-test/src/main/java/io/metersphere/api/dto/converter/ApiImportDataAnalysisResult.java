package io.metersphere.api.dto.converter;

import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * api导入数据分析结果
 */
@Data
public class ApiImportDataAnalysisResult {

    // 新增接口数据
    List<ApiDefinitionImportDetail> insertApiList = new ArrayList<>();
    // 存在的接口数据. Map<导入的接口 , 已存在的接口>
    Map<ApiDefinitionImportDetail, ApiDefinitionImportDetail> existenceApiMap = new HashMap<>();
    // 接口的用例数据
    Map<String, List<ApiTestCaseDTO>> apiIdAndTestCaseMap = new HashMap<>();
    // 接口的mock数据
    Map<String, List<ApiDefinitionMockDTO>> apiIdAndMockMap = new HashMap<>();
}
