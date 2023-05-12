package io.metersphere.sdk.domain;

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

@ApiModel(value = "执行链条目")
@TableName("execution_queue_detail")
@Data
public class ExecutionQueueDetail implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** ID */
    @TableId
    @NotBlank(message = "ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "ID")
    private String id;
    
    /** 队列id */
    
    
    @ApiModelProperty(name = "队列id")
    private String queueId;
    
    /** 排序 */
    
    
    @ApiModelProperty(name = "排序")
    private Integer sort;
    
    /** 报告id */
    
    
    @ApiModelProperty(name = "报告id")
    private String reportId;
    
    /** 资源id */
    
    
    @ApiModelProperty(name = "资源id")
    private String testId;
    
    /** 环境 */
    
    
    @ApiModelProperty(name = "环境")
    private String evnMap;
    
    /** 资源类型 */
    
    
    @ApiModelProperty(name = "资源类型")
    private String type;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /** 是否开启失败重试 */
    
    
    @ApiModelProperty(name = "是否开启失败重试")
    private Boolean retryEnable;
    
    /** 失败重试次数 */
    
    
    @ApiModelProperty(name = "失败重试次数")
    private Long retryNumber;
    
    /** 项目ID集合 */
    
    
    @ApiModelProperty(name = "项目ID集合")
    private String projectIds;
    

}