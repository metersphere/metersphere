package io.metersphere.api.dto.request.processors;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.api.dto.request.http.KeyValueParam;
import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.system.valid.EnumValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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
     * {@link ScriptLanguageType}
     */
    @Size(max = 20)
    @EnumValue(enumClass = ScriptLanguageType.class)
    private String scriptLanguage;
    /**
     * 是否启用公共脚本
     * 默认为 false
     */
    private Boolean enableCommonScript = false;
    /**
     * 公共脚本ID
     */
    @Size(max = 50)
    private String scriptId;
    /**
     * 公共脚本入参
     */
    @Valid
    private List<KeyValueParam> params;
}
