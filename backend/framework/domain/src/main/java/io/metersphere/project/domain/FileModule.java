package io.metersphere.project.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class FileModule implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_module.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{file_module.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "项目ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_module.project_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{file_module.project_id.length_range}", groups = {Created.class, Updated.class})
    private String projectId;

    @Schema(title = "模块名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{file_module.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{file_module.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "父级ID")
    private String parentId;

    @Schema(title = "层数")
    private Integer level;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "排序用的标识")
    private Double pos;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "模块类型: module/repository")
    private String moduleType;

    private static final long serialVersionUID = 1L;
}