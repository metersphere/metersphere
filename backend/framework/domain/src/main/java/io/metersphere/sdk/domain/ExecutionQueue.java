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

@ApiModel(value = "执行链（接口/场景/UI/性能用例）")
@TableName("execution_queue")
@Data
public class ExecutionQueue implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** ID */
    @TableId
    @NotBlank(message = "ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "ID")
    private String id;
    
    /** 集合报告/测试计划报告 */
    
    
    @ApiModelProperty(name = "集合报告/测试计划报告")
    private String reportId;
    
    /** 报告类型/计划报告/单独报告 */
    
    
    @ApiModelProperty(name = "报告类型/计划报告/单独报告")
    private String reportType;
    
    /** 执行模式/scenario/api/test_paln_api/test_pan_scenario */
    
    
    @ApiModelProperty(name = "执行模式/scenario/api/test_paln_api/test_pan_scenario")
    private String runMode;
    
    /** 执行资源池 */
    
    
    @ApiModelProperty(name = "执行资源池")
    private String poolId;
    
    /** 创建时间 */
    
    
    @ApiModelProperty(name = "创建时间")
    private Long createTime;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private Boolean failure;
    

}