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

@ApiModel(value = "新手村")
@TableName("novice_statistics")
@Data
public class NoviceStatistics implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{novice_statistics.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "用户id", required = false, allowableValues = "range[1, 64]")
    private String userId;

    @Size(min = 1, max = 1, message = "{novice_statistics.guide_step.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{novice_statistics.guide_step.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "新手引导完成的步骤", required = true, allowableValues = "range[1, 1]")
    private Boolean guideStep;


    @ApiModelProperty(name = "新手引导的次数", required = true, allowableValues = "range[1, ]")
    private Integer guideNum;


    @ApiModelProperty(name = "data option (JSON format)", required = false, allowableValues = "range[1, ]")
    private byte[] dataOption;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, ]")
    private Long createTime;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, ]")
    private Long updateTime;


}