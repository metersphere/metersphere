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
    
    /** 项目ID */
    @TableId
    @NotBlank(message = "项目ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "项目ID")
    private String projectId;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private String tapdId;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private String jiraKey;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private String zentaoId;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private String azureDevopsId;
    
    /** 用例模版ID */
    
    
    @ApiModelProperty(name = "用例模版ID")
    private String caseTemplateId;
    
    /** azure 过滤需求的 parent workItem ID */
    
    
    @ApiModelProperty(name = "azure 过滤需求的 parent workItem ID")
    private String azureFilterId;
    
    /** 项目使用哪个平台的模板 */
    @Size(min = 1, max = 20, message = "项目使用哪个平台的模板长度必须在1-20之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "项目使用哪个平台的模板不能为空", groups = {Created.class})
    @ApiModelProperty(name = "项目使用哪个平台的模板")
    private String platform;
    
    /** 是否使用第三方平台缺陷模板 */
    
    
    @ApiModelProperty(name = "是否使用第三方平台缺陷模板")
    private Boolean thirdPartTemplate;
    
    /** 是否开启版本管理 */
    
    
    @ApiModelProperty(name = "是否开启版本管理")
    private Boolean versionEnable;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private byte[] issueConfig;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private String apiTemplateId;
    

}