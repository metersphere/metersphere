package io.metersphere.project.domain;

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

@ApiModel(value = "功能用例模版")
@Table("functional_case_template")
@Data
public class FunctionalCaseTemplate implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @NotBlank(message = "{functional_case_template.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues="range[1, 50]")
    private String id;
    
    @Size(min = 1, max = 64, message = "{functional_case_template.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_template.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "名称", required = true, allowableValues="range[1, 64]")
    private String name;
    
    
    
    @ApiModelProperty(name = "描述", required = false, allowableValues="range[1, 255]")
    private String description;
    
    
    
    @ApiModelProperty(name = "是否是系统模板", required = true, allowableValues="range[1, ]")
    private Boolean system;
    
    
    
    @ApiModelProperty(name = "创建时间", required = true, allowableValues="range[1, ]")
    private Long createTime;
    
    @Size(min = 1, max = 50, message = "{functional_case_template.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_template.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues="range[1, 50]")
    private String createUser;
    
    @Size(min = 1, max = 50, message = "{functional_case_template.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_template.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues="range[1, 50]")
    private String projectId;
    

}