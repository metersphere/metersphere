package io.metersphere.project.domain;

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

@ApiModel(value = "误报库")
@Table("fake_error")
@Data
public class FakeError implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{fake_error.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "误报ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{fake_error.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{fake_error.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues = "range[1, 50]")
    private String projectId;


    @ApiModelProperty(name = "创建时间", required = true, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, allowableValues = "range[1, ]")
    private Long updateTime;

    @Size(min = 1, max = 64, message = "{fake_error.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{fake_error.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 64]")
    private String createUser;

    @Size(min = 1, max = 64, message = "{fake_error.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{fake_error.update_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "更新人", required = true, allowableValues = "range[1, 64]")
    private String updateUser;

    @Size(min = 1, max = 255, message = "{fake_error.error_code.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{fake_error.error_code.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "错误码", required = true, allowableValues = "range[1, 255]")
    private String errorCode;

    @Size(min = 1, max = 255, message = "{fake_error.match_type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{fake_error.match_type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "匹配类型", required = true, allowableValues = "range[1, 255]")
    private String matchType;


    @ApiModelProperty(name = "状态", required = false, allowableValues = "range[1, 1]")
    private Boolean status;


}