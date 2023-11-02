package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author wx
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FunctionalCasePageDTO implements Serializable {


    @Schema(description = "自定义字段集合")
    private List<CaseCustomsFieldDTO> customsFields;
}
