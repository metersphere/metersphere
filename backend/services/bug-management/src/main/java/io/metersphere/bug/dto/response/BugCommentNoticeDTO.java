package io.metersphere.bug.dto.response;

import io.metersphere.system.dto.sdk.OptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class BugCommentNoticeDTO extends BugDTO {

    @Schema(description = "通知人集合, @用户, 使用';'隔开! ")
    private String notifier;

    @Schema(description = "自定义字段值集合")
    private List<OptionDTO> fields;
}
