package io.metersphere.project.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class BugTemplateExtend implements Serializable {
    @Schema(title = "缺陷模板ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_template_extend.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{bug_template_extend.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "缺陷标题模板")
    private String title;

    @Schema(title = "缺陷内容模板")
    private String content;

    private static final long serialVersionUID = 1L;
}