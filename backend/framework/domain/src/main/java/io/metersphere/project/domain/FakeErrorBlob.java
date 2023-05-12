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

@ApiModel(value = "误报库大字段")
@TableName("fake_error_blob")
@Data
public class FakeErrorBlob implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** Test ID */
    @TableId
    @NotBlank(message = "Test ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "Test ID")
    private String fakeErrorId;
    
    /** 内容 */
    
    
    @ApiModelProperty(name = "内容")
    private byte[] content;
    
    /** 报告内容 */
    
    
    @ApiModelProperty(name = "报告内容")
    private byte[] description;
    

}