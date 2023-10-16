package io.metersphere.bug.dto;

import io.metersphere.bug.domain.Bug;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author song-cc-rock
 */
@Data
public class BugDTO extends Bug {

    @Schema(description = "缺陷内容")
    private String description;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "指派人名称")
    private String assignUserName;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "关联用例数量")
    private Integer relationCaseCount;

    @Schema(description = "自定义字段集合")
    private List<BugCustomFieldDTO> customFields;
}
