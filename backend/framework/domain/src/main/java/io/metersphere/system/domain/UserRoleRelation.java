package io.metersphere.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "用户组关系")
@TableName("user_role_relation")
@Data
public class UserRoleRelation implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{user_role_relation.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "用户组关系ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{user_role_relation.user_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user_role_relation.user_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "用户ID", required = true, allowableValues = "range[1, 50]")
    private String userId;

    @Size(min = 1, max = 50, message = "{user_role_relation.role_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user_role_relation.role_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "组ID", required = true, allowableValues = "range[1, 50]")
    private String roleId;

    @Size(min = 1, max = 50, message = "{user_role_relation.source_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{user_role_relation.source_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "工作空间或项目ID", required = true, allowableValues = "range[1, 50]")
    private String sourceId;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, allowableValues = "range[1, ]")
    private Long updateTime;


}