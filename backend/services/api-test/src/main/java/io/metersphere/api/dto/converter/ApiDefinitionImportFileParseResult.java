package io.metersphere.api.dto.converter;

import io.metersphere.api.dto.definition.ApiDefinitionMockDTO;
import io.metersphere.api.dto.definition.ApiTestCaseDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * api导入文件解析结果
 */
@Data
public class ApiDefinitionImportFileParseResult {
    // 接口定义数据
    private List<ApiDefinitionDetail> data = new ArrayList<>();
    // 用例数据
    private Map<String, List<ApiTestCaseDTO>> caseMap = new HashMap<>();
    // mock数据
    private Map<String, List<ApiDefinitionMockDTO>> mockMap = new HashMap<>();

    public List<String> getApiProtocols() {
        List<String> protocols = new ArrayList<>();
        for (ApiDefinitionDetail apiDefinitionDetail : data) {
            if (!protocols.contains(apiDefinitionDetail.getProtocol())) {
                protocols.add(apiDefinitionDetail.getProtocol());
            }
        }
        return protocols;
    }
}
