package io.metersphere.functional.dto;

import io.metersphere.functional.domain.FunctionalCase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author wx
 */
@Data
public class FunctionalCasePageDTO extends FunctionalCase {


    @Schema(description = "自定义字段集合")
    private List<FunctionalCaseCustomFieldDTO> customFields;
}
