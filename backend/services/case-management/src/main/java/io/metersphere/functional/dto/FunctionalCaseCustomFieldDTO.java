package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
public class FunctionalCaseCustomFieldDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description =  "用例ID")
    private String caseId;

    @Schema(description =  "字段ID")
    private String fieldId;

    @Schema(description =  "字段值")
    private String value;

    @Schema(description =  "字段名称")
    private String name;

    @Schema(description = "是否内置字段")
    private Boolean internal;

}
