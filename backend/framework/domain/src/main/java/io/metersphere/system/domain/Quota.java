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

    @TableId
    @NotBlank(message = "{quota.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "接口数量", required = false, allowableValues = "range[1, ]")
    private Integer api;


    @ApiModelProperty(name = "性能测试数量", required = false, allowableValues = "range[1, ]")
    private Integer performance;


    @ApiModelProperty(name = "最大并发数", required = false, allowableValues = "range[1, ]")
    private Integer maxThreads;


    @ApiModelProperty(name = "最大执行时长", required = false, allowableValues = "range[1, ]")
    private Integer duration;


    @ApiModelProperty(name = "资源池列表", required = false, allowableValues = "range[1, 1000]")
    private String resourcePool;


    @ApiModelProperty(name = "工作空间ID", required = false, allowableValues = "range[1, 50]")
    private String workspaceId;


    @ApiModelProperty(name = "是否使用默认值", required = false, allowableValues = "range[1, 1]")
    private Boolean useDefault;


    @ApiModelProperty(name = "更新时间", required = false, allowableValues = "range[1, ]")
    private Long updateTime;


    @ApiModelProperty(name = "成员数量限制", required = false, allowableValues = "range[1, ]")
    private Integer member;


    @ApiModelProperty(name = "项目数量限制", required = false, allowableValues = "range[1, ]")
    private Integer project;


    @ApiModelProperty(name = "项目类型配额", required = false, allowableValues = "range[1, 50]")
    private String projectId;


    @ApiModelProperty(name = "总vum数", required = false, allowableValues = "range[1, 10]")
    private Double vumTotal;


    @ApiModelProperty(name = "消耗的vum数", required = false, allowableValues = "range[1, 10]")
    private Double vumUsed;


}