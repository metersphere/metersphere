package io.metersphere.project.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class FunctionalCaseTemplate implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_template.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case_template.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_template.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 255, message = "{functional_case_template.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "描述")
    private String description;

    @Schema(title = "是否是系统模板", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_template.system.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 1, message = "{functional_case_template.system.length_range}", groups = {Created.class, Updated.class})
    private Boolean system;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_template.create_user.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_template.create_user.length_range}", groups = {Created.class, Updated.class})
    private String createUser;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_template.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_template.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    private static final long serialVersionUID = 1L;
}