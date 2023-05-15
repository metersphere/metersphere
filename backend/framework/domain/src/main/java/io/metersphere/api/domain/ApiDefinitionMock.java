
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

@ApiModel(value = "mock 配置")
@TableName("api_definition_mock")
@Data
public class ApiDefinitionMock implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_definition_mock.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "mock pk", required = true, allowableValues = "range[1, 50]")
    private String id;

    @ApiModelProperty(name = "接口路径", required = false, allowableValues = "range[1, 1000]")
    private String apiPath;

    @ApiModelProperty(name = "接口类型", required = false, allowableValues = "range[1, 64]")
    private String apiMethod;

    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @ApiModelProperty(name = "修改时间", required = true, dataType = "Long")
    private Long updateTime;

    @Size(min = 1, max = 64, message = "{api_definition_mock.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_mock.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 64]")
    private String createUser;

    @Size(min = 1, max = 200, message = "{api_definition_mock.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_mock.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "mock 名称", required = true, allowableValues = "range[1, 200]")
    private String name;

    @ApiModelProperty(name = "自定义标签", required = false, allowableValues = "range[1, 1000]")
    private String tags;

    @ApiModelProperty(name = "状态", required = false, allowableValues = "range[1, 10]")
    private String status;

    @Size(min = 1, max = 50, message = "{api_definition_mock.expect_num.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_mock.expect_num.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "mock编号", required = true, allowableValues = "range[1, 50]")
    private String expectNum;

    @Size(min = 1, max = 50, message = "{api_definition_mock.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_mock.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目fk", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @Size(min = 1, max = 50, message = "{api_definition_mock.api_definition_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_mock.api_definition_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "接口fk", required = true, allowableValues = "range[1, 50]")
    private String apiDefinitionId;


}