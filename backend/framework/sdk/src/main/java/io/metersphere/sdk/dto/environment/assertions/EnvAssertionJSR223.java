package io.metersphere.sdk.dto.environment.assertions;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class EnvAssertionJSR223 extends EnvAssertionType {
    @Schema(description = "变量名")
    private String variable;
    @Schema(description = "操作符")
    private String operator;
    @Schema(description = "值")
    private String value;
    @Schema(description = "脚本名称")
    private String name;
    @Schema(description = "脚本内容")
    private String script;
    @Schema(description = "脚本语言")
    private String scriptLanguage;
    @Schema(description = "是否启用jsr223")
    private Boolean jsrEnable;

    public EnvAssertionJSR223() {
        setType(JSR223);
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(script) && StringUtils.isNotBlank(scriptLanguage) && isEnable();
    }
}
