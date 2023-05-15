
package io.metersphere.api.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "接口定义详情内容")
@TableName("api_definition_blob")
@Data
public class ApiDefinitionBlob implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_definition_blob.api_definition_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "接口fk/ 一对一关系", required = true, allowableValues = "range[1, 50]")
    private String apiDefinitionId;


    @ApiModelProperty(name = "请求内容", required = false, dataType = "byte[]")
    private byte[] request;


    @ApiModelProperty(name = "响应内容", required = false, dataType = "byte[]")
    private byte[] response;


    @ApiModelProperty(name = "备注", required = false, dataType = "byte[]")
    private byte[] remark;


}