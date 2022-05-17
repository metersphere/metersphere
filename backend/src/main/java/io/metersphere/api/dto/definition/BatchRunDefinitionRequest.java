package io.metersphere.api.dto.definition;

import io.metersphere.base.domain.ApiDefinitionExecResultWithBLOBs;
import io.metersphere.dto.RunModeConfigDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class BatchRunDefinitionRequest {
    private String id;

    private List<String> planIds;

    private String triggerMode;

    private RunModeConfigDTO config;

    private String userId;

    //测试计划报告ID。 测试计划执行时使用
    private String planReportId;
    // 失败重跑
    private boolean rerun;
    private Map<String, ApiDefinitionExecResultWithBLOBs> executeQueue;

}
