package io.metersphere.api.dto.converter;

import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * api导入数据分析结果
 */
@Data
public class ApiDefinitionImportDataAnalysisResult {

    // 新增接口数据
    List<ApiDefinitionDetail> insertApiList = new ArrayList<>();
    // 存在的接口数据
    List<ExistenceApiDefinitionDetail> existenceApiList = new ArrayList<>();
    // 接口的用例数据
    Map<String, List<ApiTestCaseDTO>> apiIdAndTestCaseMap = new HashMap<>();
    // 接口的mock数据
    Map<String, List<ApiDefinitionMockDTO>> apiIdAndMockMap = new HashMap<>();

    public void addExistenceApi(ApiDefinitionDetail importApi, List<ApiDefinitionDetail> exportApiList) {
        this.existenceApiList.add(new ExistenceApiDefinitionDetail(importApi, exportApiList));
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(insertApiList) && CollectionUtils.isEmpty(existenceApiList);
    }
}