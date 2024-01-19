package io.metersphere.project.dto.environment.processors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.project.dto.environment.KeyValueParam;
import lombok.Data;

import java.util.List;

@Data
@JsonTypeName("ENV_SCRIPT")
public class ScriptProcessor extends MsProcessor {
    /**
     * 脚本内容
     */
    private String script;
    /**
     * 脚本语言
     *
     * @see io.metersphere.project.constants.ScriptLanguageType
     */
    private String scriptLanguage;
    /**
     * 是否启用公共脚本
     */
    private Boolean enableCommonScript = false;
    /**
     * 脚本ID
     */
    private String scriptId;
    /**
     * 公共脚本入参
     */
    private List<KeyValueParam> params;
}
