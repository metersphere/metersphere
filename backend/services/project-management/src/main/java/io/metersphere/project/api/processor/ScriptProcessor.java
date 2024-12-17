package io.metersphere.project.api.processor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.project.dto.CommonScriptInfo;
import io.metersphere.sdk.valid.EnumValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;


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
    @NotBlank
    @EnumValue(enumClass = ScriptLanguageType.class)
    private String scriptLanguage;
    /**
     * 是否启用公共脚本
     * 默认为 false
     * 环境脚本无须配置
     */
    private Boolean enableCommonScript = false;
    /**
     * 公共脚本信息
     * {@link CommonScriptInfo}
     */
    @Valid
    private CommonScriptInfo commonScriptInfo;

    @JsonIgnore
    public boolean isValid() {
        if (isEnableCommonScript()) {
            return commonScriptInfo != null && StringUtils.isNotBlank(commonScriptInfo.getId());
        } else {
            return StringUtils.isNotBlank(script);
        }
    }

    public boolean isEnableCommonScript() {
        return BooleanUtils.isTrue(enableCommonScript);
    }
}
