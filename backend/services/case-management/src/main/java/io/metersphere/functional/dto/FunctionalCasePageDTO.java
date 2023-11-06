package io.metersphere.functional.dto;

import io.metersphere.functional.domain.FunctionalCase;
import io.metersphere.functional.domain.FunctionalCaseCustomField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author wx
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FunctionalCasePageDTO extends FunctionalCase {


    @Schema(description = "自定义字段集合")
    private List<FunctionalCaseCustomField> customsFields;
}
