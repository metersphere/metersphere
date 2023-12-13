package io.metersphere.api.dto.request.processors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  21:12
 */
@Data
@JsonTypeName("SCRIPT")
public class ScriptProcessor extends MsProcessor {
    private String script;
    private String scriptLanguage;
    private Boolean jsrEnable;
}
