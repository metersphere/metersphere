package io.metersphere.project.dto.environment.assertion;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.project.dto.environment.KeyValueParam;
import io.metersphere.project.dto.environment.processors.ScriptProcessor;
import lombok.Data;

import java.util.List;

@Data
@JsonTypeName("ENV_SCRIPT_ASSERTION")
public class MsScriptAssertion extends MsAssertion {
    /**
     * 脚本内容
     */
    private String script;
    /**
     * 脚本语言
     *
     * @see ScriptProcessor.ScriptLanguageType
     */
    private String scriptLanguage;
    /**
     * 是否启用公共脚本
     */
    private Boolean enableCommonScript;
    /**
     * 脚本ID
     */
    private String scriptId;
    /**
     * 公共脚本入参
     */
    private List<KeyValueParam> params;
}
