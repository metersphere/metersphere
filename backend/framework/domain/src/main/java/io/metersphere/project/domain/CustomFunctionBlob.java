package io.metersphere.project.domain;

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

@ApiModel(value = "自定义函数-代码片段大字段")
@TableName("custom_function_blob")
@Data
public class CustomFunctionBlob implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**  */
    @TableId
    @NotBlank(message = "不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "")
    private String functionId;
    
    /** 参数列表 */
    
    
    @ApiModelProperty(name = "参数列表")
    private byte[] params;
    
    /** 函数体 */
    
    
    @ApiModelProperty(name = "函数体")
    private byte[] script;
    
    /** 执行结果 */
    
    
    @ApiModelProperty(name = "执行结果")
    private byte[] result;
    

}