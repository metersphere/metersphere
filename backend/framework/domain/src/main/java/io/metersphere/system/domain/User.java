package io.metersphere.system.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@ApiModel(value = "用户")
@Table("user")
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{user.id.not_blank}", groups = {Created.class, Updated.class})
    @ApiModelProperty(name = "用户ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 64, message = "{user.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "用户名", required = true, allowableValues = "range[1, 64]")
    private String name;

    @Size(min = 1, max = 64, message = "{user.email.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user.email.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "用户邮箱", required = true, allowableValues = "range[1, 64]")
    private String email;


    @ApiModelProperty(name = "用户密码", required = false, allowableValues = "range[1, 256]")
    private String password;

    @Size(min = 1, max = 50, message = "{user.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "用户状态，启用或禁用", required = true, allowableValues = "range[1, 50]")
    private String status;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, allowableValues = "range[1, ]")
    private Long updateTime;


    @ApiModelProperty(name = "语言", required = false, allowableValues = "range[1, 30]")
    private String language;


    @ApiModelProperty(name = "当前工作空间ID", required = false, allowableValues = "range[1, 50]")
    private String lastWorkspaceId;


    @ApiModelProperty(name = "手机号", required = false, allowableValues = "range[1, 50]")
    private String phone;

    @Size(min = 1, max = 50, message = "{user.source.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user.source.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "来源：LOCAL OIDC CAS", required = true, allowableValues = "range[1, 50]")
    private String source;


    @ApiModelProperty(name = "当前项目ID", required = false, allowableValues = "range[1, 50]")
    private String lastProjectId;

    @Size(min = 1, max = 100, message = "{user.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 100]")
    private String createUser;


}