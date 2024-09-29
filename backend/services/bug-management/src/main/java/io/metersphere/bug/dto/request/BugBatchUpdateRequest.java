package io.metersphere.bug.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class BugBatchUpdateRequest extends BugBatchRequest{

    @Schema(description = "标签内容")
    private List<String> tags;

    @Schema(description = "是否追加", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean append;

    @Schema(description = "是否清空")
    private boolean clear;

    @Schema(description = "更新人")
    private String updateUser;

    @Schema(description = "更新时间")
    private Long updateTime;
}
