package io.metersphere.project.api.processor;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.metersphere.project.dto.environment.processors.RequestScriptProcessor;
import io.metersphere.project.dto.environment.processors.ScenarioScriptProcessor;
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
 *   SCENARIO_SCRIPT {@link ScenarioScriptProcessor}
 *   REQUEST_SCRIPT {@link RequestScriptProcessor}
 * <pre>
 * @Author: jianxing
 * @CreateTime: 2023-11-07  10:17
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "processorType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ScriptProcessor.class),
        @JsonSubTypes.Type(value = ScenarioScriptProcessor.class),
        @JsonSubTypes.Type(value = RequestScriptProcessor.class),
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
}
