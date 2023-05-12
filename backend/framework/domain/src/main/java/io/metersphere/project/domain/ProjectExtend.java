package io.metersphere.project.domain;

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

@ApiModel(value = "项目扩展")
@TableName("project_extend")
@Data
public class ProjectExtend implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{project_extend.project_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues = "range[1, 50]")
    private String projectId;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, 50]")
    private String tapdId;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, 50]")
    private String jiraKey;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, 50]")
    private String zentaoId;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, 50]")
    private String azureDevopsId;


    @ApiModelProperty(name = "用例模版ID", required = false, allowableValues = "range[1, 50]")
    private String caseTemplateId;


    @ApiModelProperty(name = "azure 过滤需求的 parent workItem ID", required = false, allowableValues = "range[1, 50]")
    private String azureFilterId;

    @Size(min = 1, max = 20, message = "{project_extend.platform.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{project_extend.platform.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目使用哪个平台的模板", required = true, allowableValues = "range[1, 20]")
    private String platform;


    @ApiModelProperty(name = "是否使用第三方平台缺陷模板", required = false, allowableValues = "range[1, 1]")
    private Boolean thirdPartTemplate;


    @ApiModelProperty(name = "是否开启版本管理", required = false, allowableValues = "range[1, 1]")
    private Boolean versionEnable;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, ]")
    private byte[] issueConfig;


    @ApiModelProperty(name = "", required = false, allowableValues = "range[1, 64]")
    private String apiTemplateId;


}