package io.metersphere.api.dto.definition.parse;

import io.metersphere.api.dto.definition.parse.ms.NodeTree;
import io.metersphere.base.domain.ApiDefinitionWithBLOBs;
import io.metersphere.base.domain.ApiTestCaseWithBLOBs;
import io.metersphere.base.domain.EsbApiParamsWithBLOBs;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ApiDefinitionImport {
    private String projectName;
    private String protocol;
    private List<ApiDefinitionWithBLOBs> data;

    // 新版本带用例导出
    private List<ApiTestCaseWithBLOBs> cases;

    //ESB文件导入的附属数据类
    private Map<String,EsbApiParamsWithBLOBs> esbApiParamsMap;

    private List<NodeTree> nodeTree;
}
