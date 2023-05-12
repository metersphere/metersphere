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

@ApiModel(value = "自定义字段关系")
@TableName("custom_field_api")
@Data
public class CustomFieldApi implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 资源ID */
    @TableId
    @NotBlank(message = "资源ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "资源ID")
    private String resourceId;
    
    /** 字段ID */
    @TableId
    @NotBlank(message = "字段ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "字段ID")
    private String fieldId;
    
    /** 字段值 */
    
    
    @ApiModelProperty(name = "字段值")
    private String value;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private byte[] textValue;
    

}