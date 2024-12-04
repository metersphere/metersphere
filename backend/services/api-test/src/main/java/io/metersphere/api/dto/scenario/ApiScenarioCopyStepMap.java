package io.metersphere.api.dto.scenario;

import lombok.Data;

import java.util.Map;

/**
 * @Author: jianxing
 * @CreateTime: 2024-01-10  11:24
 */
@Data
public class ApiScenarioCopyStepMap {

    /**
     * key 的 stepId，value 为 copyFrom 的步骤ID
     */
    private Map<String, String> copyFromStepIdMap;
    /**
     * key 的 stepId，value 为 copyFrom 的接口ID
     */
    private Map<String, String> isNewApiResourceMap;
    /**
     * key 的 stepId，value 为 copyFrom 的接口用例ID
     */
    private Map<String, String> isNewApiCaseResourceMap;
}
