package io.metersphere.project.api.assertion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.metersphere.project.constants.ScriptLanguageType;
import io.metersphere.project.dto.CommonScriptInfo;
import jakarta.validation.Valid;
import lombok.Data;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

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
     * {@link ScriptLanguageType}
     */
    private String scriptLanguage;
    /**
     * 是否启用公共脚本
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
