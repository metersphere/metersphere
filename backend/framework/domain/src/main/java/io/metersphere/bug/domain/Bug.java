package io.metersphere.bug.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class Bug implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{bug.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "业务ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{bug.num.not_blank}", groups = {Created.class})
    private Integer num;

    @Schema(title = "缺陷标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.title.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 300, message = "{bug.title.length_range}", groups = {Created.class, Updated.class})
    private String title;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "缺陷平台", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.platform.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug.platform.length_range}", groups = {Created.class, Updated.class})
    private String platform;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "缺陷来源，记录创建该缺陷的测试计划的ID")
    private String sourceId;

    @Schema(title = "第三方平台状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.platform_status.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug.platform_status.length_range}", groups = {Created.class, Updated.class})
    private String platformStatus;

    @Schema(title = "第三方平台缺陷ID")
    private String platformId;

    private static final long serialVersionUID = 1L;
}