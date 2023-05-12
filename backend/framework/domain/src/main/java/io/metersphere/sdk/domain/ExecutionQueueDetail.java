package io.metersphere.sdk.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "执行链条目")
@TableName("execution_queue_detail")
@Data
public class ExecutionQueueDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{execution_queue_detail.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "队列id", required = false, allowableValues = "range[1, 100]")
    private String queueId;


    @ApiModelProperty(name = "排序", required = false, allowableValues = "range[1, ]")
    private Integer sort;


    @ApiModelProperty(name = "报告id", required = false, allowableValues = "range[1, 100]")
    private String reportId;


    @ApiModelProperty(name = "资源id", required = false, allowableValues = "range[1, 100]")
    private String testId;


    @ApiModelProperty(name = "环境", required = false, allowableValues = "range[1, ]")
    private String evnMap;


    @ApiModelProperty(name = "资源类型", required = false, allowableValues = "range[1, 100]")
    private String type;


    @ApiModelProperty(name = "创建时间", required = false, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "是否开启失败重试", required = false, allowableValues = "range[1, 1]")
    private Boolean retryEnable;


    @ApiModelProperty(name = "失败重试次数", required = false, allowableValues = "range[1, ]")
    private Long retryNumber;


    @ApiModelProperty(name = "项目ID集合", required = false, allowableValues = "range[1, 2000]")
    private String projectIds;


}