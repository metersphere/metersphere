package io.metersphere.sdk.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "执行链（接口/场景/UI/性能用例）")
@TableName("execution_queue")
@Data
public class ExecutionQueue implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{execution_queue.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "集合报告/测试计划报告", required = false, allowableValues = "range[1, 100]")
    private String reportId;


    @ApiModelProperty(name = "报告类型/计划报告/单独报告", required = false, allowableValues = "range[1, 100]")
    private String reportType;


    @ApiModelProperty(name = "执行模式/scenario/api/test_paln_api/test_pan_scenario", required = false, allowableValues = "range[1, 100]")
    private String runMode;


    @ApiModelProperty(name = "执行资源池", required = false, allowableValues = "range[1, 100]")
    private String poolId;


    @ApiModelProperty(name = "创建时间", required = false, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, 1]")
    private Boolean failure;


}