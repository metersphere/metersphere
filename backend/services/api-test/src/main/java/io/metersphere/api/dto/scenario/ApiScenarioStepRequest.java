package io.metersphere.api.dto.scenario;

import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-10  11:24
 */
@Data
public class ApiScenarioStepRequest extends ApiScenarioStepCommonDTO<ApiScenarioStepRequest> {
    /**
     * 记录是从哪个步骤复制来的
     * 如果没有传步骤详情
     * 保存时需要根据这个字段查询原步骤详情保存
     */
    private String copyFromStepId;
    /**
     * 记录当前步骤是否是新增的步骤
     */
    private Boolean isNew;
}
