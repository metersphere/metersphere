package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author wx
 */
@Data
public class CaseCustomFieldDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "字段id")
    @NotBlank(message = "{functional_case_custom_field.field_id.not_blank}")
    private String fieldId;

    @Schema(description = "自定义字段值")
    private String value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaseCustomFieldDTO that = (CaseCustomFieldDTO) o;
        return Objects.equals(fieldId, that.fieldId) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldId, value);
    }
}
