package io.metersphere.api.dto.request.assertion;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.api.dto.request.http.KeyValueParam;
import io.metersphere.api.dto.request.processors.ScriptProcessor;
import lombok.Data;

import java.util.List;

/**
 * 变量断言
 * @Author: jianxing
 * @CreateTime: 2023-11-22  15:33
 */
@Data
@JsonTypeName("SCRIPT")
public class MsScriptAssertion extends MsAssertion {
    /**
     * 脚本内容
     */
    private String script;
    /**
     * 脚本语言
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
