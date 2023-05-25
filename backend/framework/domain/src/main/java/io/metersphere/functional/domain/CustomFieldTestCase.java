package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "自定义字段功能用例关系")
@Table("custom_field_test_case")
@Data
public class CustomFieldTestCase implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{custom_field_test_case.resource_id.not_blank}", groups = {Updated.class})
    @Schema(title = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String resourceId;

    @NotBlank(message = "{custom_field_test_case.field_id.not_blank}", groups = {Updated.class})
    @Schema(title = "字段ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fieldId;


    @Schema(title = "字段值", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String value;


    @Schema(title = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String textValue;


}