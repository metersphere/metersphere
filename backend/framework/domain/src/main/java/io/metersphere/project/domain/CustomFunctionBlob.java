package io.metersphere.project.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@ApiModel(value = "自定义函数-代码片段大字段")
@TableName("custom_function_blob")
@Data
@EqualsAndHashCode(callSuper=false)
public class CustomFunctionBlob implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{custom_function_blob.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "参数列表", required = false, allowableValues = "range[1, ]")
    private byte[] params;


    @ApiModelProperty(name = "函数体", required = false, allowableValues = "range[1, ]")
    private byte[] script;


    @ApiModelProperty(name = "执行结果", required = false, allowableValues = "range[1, ]")
    private byte[] result;


}