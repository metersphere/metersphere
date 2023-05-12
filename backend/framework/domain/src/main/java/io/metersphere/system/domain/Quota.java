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

@ApiModel(value = "配额")
@TableName("quota")
@Data
public class Quota implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**  */
    @TableId
    @NotBlank(message = "不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "")
    private String id;
    
    /** 接口数量 */
    
    
    @ApiModelProperty(name = "接口数量")
    private Integer api;
    
    /** 性能测试数量 */
    
    
    @ApiModelProperty(name = "性能测试数量")
    private Integer performance;
    
    /** 最大并发数 */
    
    
    @ApiModelProperty(name = "最大并发数")
    private Integer maxThreads;
    
    /** 最大执行时长 */
    
    
    @ApiModelProperty(name = "最大执行时长")
    private Integer duration;
    
    /** 资源池列表 */
    
    
    @ApiModelProperty(name = "资源池列表")
    private String resourcePool;
    
    /** 工作空间ID */
    
    
    @ApiModelProperty(name = "工作空间ID")
    private String workspaceId;
    
    /** 是否使用默认值 */
    
    
    @ApiModelProperty(name = "是否使用默认值")
    private Boolean useDefault;
    
    /** 更新时间 */
    
    
    @ApiModelProperty(name = "更新时间")
    private Long updateTime;
    
    /** 成员数量限制 */
    
    
    @ApiModelProperty(name = "成员数量限制")
    private Integer member;
    
    /** 项目数量限制 */
    
    
    @ApiModelProperty(name = "项目数量限制")
    private Integer project;
    
    /** 项目类型配额 */
    
    
    @ApiModelProperty(name = "项目类型配额")
    private String projectId;
    
    /** 总vum数 */
    
    
    @ApiModelProperty(name = "总vum数")
    private Double vumTotal;
    
    /** 消耗的vum数 */
    
    
    @ApiModelProperty(name = "消耗的vum数")
    private Double vumUsed;
    

}