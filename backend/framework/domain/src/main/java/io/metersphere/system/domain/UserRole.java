package io.metersphere.system.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "用户组")
@Table("user_role")
@Data
public class UserRole implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{user_role.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "组ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 64, message = "{user_role.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user_role.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "组名称", required = true, allowableValues = "range[1, 64]")
    private String name;


    @ApiModelProperty(name = "描述", required = false, allowableValues = "range[1, 100]")
    private String description;

    @Size(min = 1, max = 1, message = "{user_role.system.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user_role.system.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否是系统用户组", required = true, allowableValues = "range[1, 1]")
    private Boolean system;

    @Size(min = 1, max = 20, message = "{user_role.type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user_role.type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "所属类型", required = true, allowableValues = "range[1, 20]")
    private String type;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, allowableValues = "range[1, ]")
    private Long updateTime;

    @Size(min = 1, max = 64, message = "{user_role.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user_role.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人(操作人）", required = true, allowableValues = "range[1, 64]")
    private String createUser;

    @Size(min = 1, max = 64, message = "{user_role.scope_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user_role.scope_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "应用范围", required = true, allowableValues = "range[1, 64]")
    private String scopeId;


}