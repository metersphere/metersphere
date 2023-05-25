package io.metersphere.project.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class IssueTemplateExtend implements Serializable {
    @Schema(title = "缺陷模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{issue_template_extend.template_id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 100, message = "{issue_template_extend.template_id.length_range}", groups = {Created.class, Updated.class})
    private String templateId;

    @Schema(title = "缺陷标题模板")
    private String title;

    @Schema(title = "缺陷内容模板")
    private String content;

    private static final long serialVersionUID = 1L;
}