
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

@ApiModel(value = "定时同步配置")
@TableName("api_definition_swagger")
@Data
public class ApiDefinitionSwagger implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_definition_swagger.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "主键", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 500, message = "{api_definition_swagger.swagger_url.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_definition_swagger.swagger_url.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "url地址", required = true, allowableValues = "range[1, 500]")
    private String swaggerUrl;

    @ApiModelProperty(name = "模块fk", required = false, allowableValues = "range[1, 50]")
    private String moduleId;

    @ApiModelProperty(name = "模块路径", required = false, allowableValues = "range[1, 500]")
    private String modulePath;

    @ApiModelProperty(name = "鉴权配置信息", required = false, allowableValues = "range[1, ]")
    private byte[] config;

    @ApiModelProperty(name = "导入模式/覆盖/不覆盖", required = false, allowableValues = "range[1, 1]")
    private Boolean mode;

    @ApiModelProperty(name = "项目fk", required = false, allowableValues = "range[1, 50]")
    private String projectId;

    @ApiModelProperty(name = "导入版本", required = false, allowableValues = "range[1, 50]")
    private String versionId;

}