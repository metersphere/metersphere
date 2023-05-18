package io.metersphere.functional.domain;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@ApiModel(value = "功能用例评论")
@TableName("functional_case_comment")
@Data
public class FunctionalCaseComment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{functional_case_comment.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{functional_case_comment.case_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_comment.case_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "功能用例ID", required = true, allowableValues = "range[1, 50]")
    private String caseId;

    @Size(min = 1, max = 50, message = "{functional_case_comment.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_comment.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "评论人", required = true, allowableValues = "range[1, 50]")
    private String createUser;


    @ApiModelProperty(name = "评论时添加的状态：通过/不通过/重新提审/通过标准变更标记/强制通过标记/强制不通过标记/状态变更标记", required = false, allowableValues = "range[1, 64]")
    private String status;

    @Size(min = 1, max = 64, message = "{functional_case_comment.type.length_range}", groups = {Created.class, Updated.class})

    @NotBlank(message = "{functional_case_comment.type.not_blank}", groups = {Created.class})

    @ApiModelProperty(name = "评论类型：用例评论/测试计划用例评论/评审用例评论", required = true, allowableValues = "range[1, 64]")
    private String type;


    @ApiModelProperty(name = "当前评审所属的测试计划ID或评审ID", required = false, allowableValues = "range[1, 50]")
    private String belongId;


    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;


    @ApiModelProperty(name = "描述", required = false)
    private String description;
}
