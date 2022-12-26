package io.metersphere.api.parse.api;

import io.metersphere.api.dto.mock.config.MockConfigImportDTO;
import io.metersphere.api.parse.api.ms.NodeTree;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ApiDefinitionImport {
    private String projectName;
    private String protocol;
    private List<ApiDefinitionWithBLOBs> data;

    // 新版本带用例导出
    private List<ApiTestCaseWithBLOBs> cases = new ArrayList<>();

    //Mock数据相关
    private List<MockConfigImportDTO> mocks;

    private List<NodeTree> nodeTree;
}
