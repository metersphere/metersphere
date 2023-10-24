package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author wx
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CaseCustomsFieldDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "字段id")
    @NotBlank(message = "{functional_case_custom_field.field_id.not_blank}")
    private String fieldId;

    @Schema(description = "自定义字段值")
    private String value;
}
