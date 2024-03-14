package io.metersphere.project.dto.environment.variables;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.metersphere.sdk.constants.VariableTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

@Data
public class CommonVariables implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "id")
    private String id;
    @Schema(description = "变量名")
    private String key;
    @Schema(description = "变量类型 CONSTANT LIST JSON")
    private String paramType = VariableTypeConstants.CONSTANT.name();
    @Schema(description = "变量值")
    private String value;
    @Schema(description = "状态")
    private Boolean enable = true;
    @Schema(description = "描述")
    private String description;
    @Schema(description = "标签")
    private List<String> tags;


    @JsonIgnore
    public boolean isConstantValid() {
        return StringUtils.equals(this.paramType, VariableTypeConstants.CONSTANT.name()) && StringUtils.isNotEmpty(key);
    }

    @JsonIgnore
    public boolean isListValid() {
        return StringUtils.equals(this.paramType, VariableTypeConstants.LIST.name()) && StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value) && value.indexOf(",") != -1;
    }

    @JsonIgnore
    public boolean isJsonValid() {
        return StringUtils.equals(this.paramType, VariableTypeConstants.JSON.name()) && StringUtils.isNotEmpty(key);
    }

}
