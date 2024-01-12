package io.metersphere.project.dto.environment.processors;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "processorType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ScriptProcessor.class),
        @JsonSubTypes.Type(value = SQLProcessor.class),
        @JsonSubTypes.Type(value = ScenarioScript.class),
        @JsonSubTypes.Type(value = StepScript.class),
})
public abstract class MsProcessor {
    /**
     * 名称
     */
    private String name;
    /**
     * 是否启用
     */
    private Boolean enable = true;
}
