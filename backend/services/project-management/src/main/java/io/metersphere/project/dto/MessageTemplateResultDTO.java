package io.metersphere.project.dto;

import io.metersphere.system.dto.sdk.OptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class MessageTemplateResultDTO implements Serializable {

    @Schema(description = "字段来源列表")
    public List<OptionDTO> fieldSourceList;

    @Schema(description = "字段列表")
    public List<MessageTemplateFieldDTO> fieldList;
}
