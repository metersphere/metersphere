package io.metersphere.bug.dto.response;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BugDetailDTO {

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{bug.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description = "业务ID")
    private Integer num;

    @Schema(description = "缺陷标题")
    @Size(min = 1, max = 255, message = "{bug.title.length_range}", groups = {Created.class, Updated.class})
    private String title;

    @Schema(description = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.project_id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{bug.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(description = "模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug.template_id.not_blank}", groups = {Created.class, Updated.class})
    @Size(min = 1, max = 50, message = "{bug.template_id.length_range}", groups = {Created.class, Updated.class})
    private String templateId;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "缺陷内容")
    private String description;

    @Schema(description = "自定义字段集合")
    private List<BugCustomFieldDTO> customFields;

    @Schema(description = "是否平台默认模板")
    private Boolean platformDefault;

    @Schema(description = "所属平台")
    private String platform;

    @Schema(description = "是否关注")
    private Boolean followFlag;

    @Schema(description = "附件集合")
    private List<BugFileDTO> attachments;

    @Schema(description = "第三方平台缺陷ID")
    private String platformBugId;

    @Schema(description = "缺陷状态")
    private String status;

    @Schema(description = "缺陷关联的用例数")
    private long linkCaseCount;

}
