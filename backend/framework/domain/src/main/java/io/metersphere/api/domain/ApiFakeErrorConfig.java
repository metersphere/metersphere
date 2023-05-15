
package io.metersphere.api.domain;

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

@ApiModel(value = "误报库")
@TableName("api_fake_error_config")
@Data
public class ApiFakeErrorConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_fake_error_config.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "误报pk", required = true, allowableValues = "range[1, 50]")
    private String id;

    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @ApiModelProperty(name = "修改时间", required = true, dataType = "Long")
    private Long updateTime;

    @Size(min = 1, max = 50, message = "{api_fake_error_config.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_fake_error_config.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;

    @Size(min = 1, max = 50, message = "{api_fake_error_config.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_fake_error_config.update_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "修改人", required = true, allowableValues = "range[1, 50]")
    private String updateUser;

    @Size(min = 1, max = 255, message = "{api_fake_error_config.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_fake_error_config.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "误报名称", required = true, allowableValues = "range[1, 255]")
    private String name;

    @Size(min = 1, max = 255, message = "{api_fake_error_config.match_type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_fake_error_config.match_type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "匹配类型", required = true, allowableValues = "range[1, 255]")
    private String matchType;

    @ApiModelProperty(name = "状态", required = false, allowableValues = "range[1, 1]")
    private Boolean status;

    @Size(min = 1, max = 50, message = "{api_fake_error_config.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_fake_error_config.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目fk", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @ApiModelProperty(name = "描述信息", required = false, allowableValues = "range[1, 500]")
    private String description;

    @ApiModelProperty(name = "误报内容", required = false, dataType = "byte[]")
    private byte[] content;

}