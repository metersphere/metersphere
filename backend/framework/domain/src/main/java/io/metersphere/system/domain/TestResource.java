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

@ApiModel(value = "测试资源池节点")
@TableName("test_resource")
@Data
public class TestResource implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** Test resource ID */
    @TableId
    @NotBlank(message = "Test resource ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "Test resource ID")
    private String id;
    
    /** Test resource pool ID this test resource belongs to */
    @Size(min = 1, max = 50, message = "Test resource pool ID this test resource belongs to长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "Test resource pool ID this test resource belongs to不能为空", groups = {Created.class})
    @ApiModelProperty(name = "Test resource pool ID this test resource belongs to")
    private String testResourcePoolId;
    
    /** Test resource configuration */
    
    
    @ApiModelProperty(name = "Test resource configuration")
    private byte[] configuration;
    
    /** Test resource status */
    @Size(min = 1, max = 64, message = "Test resource status长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "Test resource status不能为空", groups = {Created.class})
    @ApiModelProperty(name = "Test resource status")
    private String status;
    
    /** Create timestamp */
    
    
    @ApiModelProperty(name = "Create timestamp")
    private Long createTime;
    
    /** Update timestamp */
    
    
    @ApiModelProperty(name = "Update timestamp")
    private Long updateTime;
    

}