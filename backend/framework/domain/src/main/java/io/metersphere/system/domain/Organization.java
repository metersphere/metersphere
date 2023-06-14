package io.metersphere.system.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class Organization implements Serializable {
    @Schema(title = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{organization.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{organization.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "组织编号")
    private Long num;

    @Schema(title = "组织名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{organization.name.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 100, message = "{organization.name.length_range}", groups = {Created.class, Updated.class})
    private String name;

    @Schema(title = "描述")
    private String description;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    @Schema(title = "创建人")
    private String createUser;

    @Schema(title = "修改人")
    private String updateUser;

    @Schema(title = "是否删除")
    private Boolean deleted;

    @Schema(title = "删除人")
    private String deleteUser;

    @Schema(title = "删除时间")
    private Long deleteTime;

    @Schema(title = "是否启用")
    private Boolean enable;

    private static final long serialVersionUID = 1L;
}