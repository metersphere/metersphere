package io.metersphere.bug.dto.response;

import io.metersphere.bug.domain.Bug;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author song-cc-rock
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BugDTO extends Bug {

    @Schema(description = "缺陷内容")
    private String description;

    @Schema(description = "创建人名称")
    private String createUserName;

    @Schema(description = "更新人名称")
    private String updateUserName;

    @Schema(description = "删除人名称")
    private String deleteUserName;

    @Schema(description = "处理人名称")
    private String handleUserName;

    @Schema(description = "关联用例数量")
    private Integer relationCaseCount;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "自定义字段集合")
    private List<BugCustomFieldDTO> customFields;
}
