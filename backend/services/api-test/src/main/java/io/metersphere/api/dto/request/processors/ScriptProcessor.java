package io.metersphere.api.dto.request.processors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.api.dto.request.http.KeyValueParam;
import io.metersphere.project.constants.ScriptLanguageType;
import lombok.Data;

import java.util.List;

/**
 * @Author: jianxing
 * @CreateTime: 2023-11-06  21:12
 */
@Data
@JsonTypeName("SCRIPT")
public class ScriptProcessor extends MsProcessor {
    /**
     * 脚本内容
     */
    private String script;
    /**
     * 脚本语言
     * @see ScriptLanguageType
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
