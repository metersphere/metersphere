package io.metersphere.functional.domain;

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

@ApiModel(value = "用例评审")
@TableName("case_review")
@Data
public class CaseReview implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{case_review.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 200, message = "{case_review.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "名称", required = true, allowableValues = "range[1, 200]")
    private String name;

    @Size(min = 1, max = 64, message = "{case_review.status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review.status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "评审状态：未开始/进行中/已完成/已结束/已归档", required = true, allowableValues = "range[1, 64]")
    private String status;


    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;


    @ApiModelProperty(name = "评审结束时间", required = true, dataType = "Long")
    private Long endTime;


    @ApiModelProperty(name = "描述", required = false)
    private String description;

    @Size(min = 1, max = 50, message = "{case_review.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues = "range[1, 50]")
    private String projectId;


    @ApiModelProperty(name = "标签", required = false, allowableValues = "range[1, 1000]")
    private String tags;

    @Size(min = 1, max = 50, message = "{case_review.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;

    @Size(min = 1, max = 64, message = "{case_review.review_pass_rule.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{case_review.review_pass_rule.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "评审规则：单人通过/全部通过", required = true, allowableValues = "range[1, 64]")
    private String reviewPassRule;


}