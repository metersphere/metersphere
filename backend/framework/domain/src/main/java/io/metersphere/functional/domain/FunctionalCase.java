package io.metersphere.functional.domain;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "功能用例")
@Table("functional_case")
@Data
public class FunctionalCase implements Serializable {
    private static final long serialVersionUID = 1L;


    @Id
    @NotBlank(message = "{functional_case.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "业务ID", required = true, dataType = "Integer")
    private Integer num;

    @Size(min = 1, max = 64, message = "{functional_case.custom_num.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.custom_num.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "自定义业务ID", required = true, allowableValues = "range[1, 64]")
    private String customNum;

    @Size(min = 1, max = 50, message = "{functional_case.module_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.module_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "模块ID", required = true, allowableValues = "range[1, 50]")
    private String moduleId;

    @Size(min = 1, max = 50, message = "{functional_case.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @Size(min = 1, max = 255, message = "{functional_case.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "名称", required = true, allowableValues = "range[1, 255]")
    private String name;

    @Size(min = 1, max = 64, message = "{functional_case.review_status.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.review_status.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "评审状态：未开始/进行中/已完成/已结束", required = true, allowableValues = "range[1, 64]")
    private String reviewStatus;


    @ApiModelProperty(name = "标签（JSON)", required = false, allowableValues = "range[1, 1000]")
    private String tags;

    @Size(min = 1, max = 64, message = "{functional_case.step_model.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.step_model.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "编辑模式：步骤模式/文本模式", required = true, allowableValues = "range[1, 64]")
    private String stepModel;


    @ApiModelProperty(name = "自定义排序，间隔5000", required = true, dataType = "Long")
    private Long pos;

    @Size(min = 1, max = 50, message = "{functional_case.version_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.version_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "版本ID", required = true, allowableValues = "range[1, 50]")
    private String versionId;

    @Size(min = 1, max = 50, message = "{functional_case.ref_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.ref_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "指向初始版本ID", required = true, allowableValues = "range[1, 50]")
    private String refId;

    @Size(min = 1, max = 64, message = "{functional_case.last_execute_result.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.last_execute_result.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "最近的执行结果：未执行/通过/失败/阻塞/跳过", required = true, allowableValues = "range[1, 64]")
    private String lastExecuteResult;

    @Size(min = 1, max = 1, message = "{functional_case.deleted.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.deleted.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否在回收站：0-否，1-是", required = true, allowableValues = "range[1, 1]")
    private Boolean deleted;

    @Size(min = 1, max = 1, message = "{functional_case.public_case.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.public_case.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否是公共用例：0-否，1-是", required = true, allowableValues = "range[1, 1]")
    private Boolean publicCase;

    @Size(min = 1, max = 1, message = "{functional_case.latest.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.latest.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否为最新版本：0-否，1-是", required = true, allowableValues = "range[1, 1]")
    private Boolean latest;

    @Size(min = 1, max = 100, message = "{functional_case.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 100]")
    private String createUser;


    @ApiModelProperty(name = "删除人", required = false, allowableValues = "range[1, 64]")
    private String deleteUser;


    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;


    @ApiModelProperty(name = "删除时间", required = false, dataType = "Long")
    private Long deleteTime;
}
