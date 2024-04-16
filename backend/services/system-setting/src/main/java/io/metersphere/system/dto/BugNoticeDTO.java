package io.metersphere.system.dto;

import io.metersphere.system.dto.sdk.OptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BugNoticeDTO extends BugMessageDTO{

    @Schema(description = "自定义字段内容")
    private List<OptionDTO> fields;
}
