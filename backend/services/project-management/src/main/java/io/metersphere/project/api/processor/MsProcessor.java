package io.metersphere.project.api.processor;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.metersphere.project.dto.environment.processors.EnvRequestScriptProcessor;
import io.metersphere.project.dto.environment.processors.EnvScenarioScriptProcessor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 前后置处理器配置
 * <pre>
 * 该参数传参时，需要传入 processorType 字段，用于区分是哪种认证处理器
 * processorType 取值为：
 *   SCRIPT {@link ScriptProcessor}
 *   SQL {@link SQLProcessor}
 *   TIME_WAITING {@link TimeWaitingProcessor}
 *   EXTRACT {@link ExtractPostProcessor}
 *   ENV_SCENARIO_SCRIPT {@link EnvScenarioScriptProcessor}
 *   ENV_REQUEST_SCRIPT {@link EnvRequestScriptProcessor}
 * <pre>
 * @Author: jianxing
 * @CreateTime: 2023-11-07  10:17
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "processorType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ScriptProcessor.class),
        @JsonSubTypes.Type(value = EnvScenarioScriptProcessor.class),
        @JsonSubTypes.Type(value = EnvRequestScriptProcessor.class),
        @JsonSubTypes.Type(value = SQLProcessor.class),
        @JsonSubTypes.Type(value = TimeWaitingProcessor.class),
        @JsonSubTypes.Type(value = ExtractPostProcessor.class),
})
public abstract class MsProcessor {
    /**
     * 名称
     */
    @NotBlank
    @Size(max = 100)
    private String name;
    /**
     * 是否启用
     */
    private Boolean enable = true;
    /**
     * 项目ID
     * 执行时设置
     */
    private String projectId;
    /**
     * 环境ID
     * 执行时设置
     */
    private String stepId;
}
