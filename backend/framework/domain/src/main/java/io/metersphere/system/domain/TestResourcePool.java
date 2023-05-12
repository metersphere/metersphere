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

@ApiModel(value = "测试资源池")
@TableName("test_resource_pool")
@Data
public class TestResourcePool implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 资源池ID */
    @TableId
    @NotBlank(message = "资源池ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "资源池ID")
    private String id;
    
    /** 名称 */
    @Size(min = 1, max = 64, message = "名称长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "名称不能为空", groups = {Created.class})
    @ApiModelProperty(name = "名称")
    private String name;
    
    /** 类型 */
    @Size(min = 1, max = 30, message = "类型长度必须在1-30之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "类型不能为空", groups = {Created.class})
    @ApiModelProperty(name = "类型")
    private String type;
    
    /** 描述 */
    
    
    @ApiModelProperty(name = "描述")
    private String description;
    
    /** 状态 */
    @Size(min = 1, max = 64, message = "状态长度必须在1-64之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "状态不能为空", groups = {Created.class})
    @ApiModelProperty(name = "状态")
    private String status;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 更新时间 */
    
    
    @ApiModelProperty(name = "更新时间")
    private Long updateTime;
    
    /** 性能测试镜像 */
    
    
    @ApiModelProperty(name = "性能测试镜像")
    private String image;
    
    /** 性能测试jvm配置 */
    
    
    @ApiModelProperty(name = "性能测试jvm配置")
    private String heap;
    
    /** 性能测试gc配置 */
    
    
    @ApiModelProperty(name = "性能测试gc配置")
    private String gcAlgo;
    
    /** 创建人 */
    
    
    @ApiModelProperty(name = "创建人")
    private String createUser;
    
    /** 是否用于接口测试 */
    
    
    @ApiModelProperty(name = "是否用于接口测试")
    private Boolean api;
    
    /** 是否用于性能测试 */
    
    
    @ApiModelProperty(name = "是否用于性能测试")
    private Boolean performance;
    

}