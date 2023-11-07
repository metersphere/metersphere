package io.metersphere.sdk.dto.api.request.processors;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-07  10:17
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "processorType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ScriptProcessor.class),
        @JsonSubTypes.Type(value = SQLProcessor.class),
        @JsonSubTypes.Type(value = TimeWaitingProcessor.class),
})
public abstract class MsProcessor {
}
