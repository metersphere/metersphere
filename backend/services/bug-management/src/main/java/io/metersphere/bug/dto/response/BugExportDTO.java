package io.metersphere.bug.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 缺陷导出DTO
 */
@Data
public class BugExportDTO {
    @Schema(description = "缺陷ID")
    private String id;
    @Schema(description = "缺陷名称")
    private String name;
    @Schema(description = "缺陷内容")
    private String content;
    @Schema(description = "缺陷状态")
    private String status;
    @Schema(description = "缺陷处理人")
    private List<String> handleUsers;
    @Schema(description = "自定义字段集合")
    private Map<String, String> customFields;
}
