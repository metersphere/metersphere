package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "功能用例模块")
@Table("functional_case_module")
@Data
public class FunctionalCaseModule implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{functional_case_module.id.not_blank}", groups = {Updated.class})
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Size(min = 1, max = 50, message = "{functional_case_module.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_module.project_id.not_blank}", groups = {Created.class})
    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Size(min = 1, max = 100, message = "{functional_case_module.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_module.name.not_blank}", groups = {Created.class})
    @Schema(title = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(title = "父节点ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String parentId;


    @Schema(title = "节点的层级", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer level;

    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;

    @Schema(title = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime;

    @Size(min = 1, max = 10, message = "{functional_case_module.pos.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_module.pos.not_blank}", groups = {Created.class})
    @Schema(title = "同一节点下的顺序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long pos;

    @Size(min = 1, max = 50, message = "{functional_case_module.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_module.create_user.not_blank}", groups = {Created.class})
    @Schema(title = "创建人", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createUser;
}
