package io.metersphere.functional.domain;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@ApiModel(value = "功能用例模块")
@Table("functional_case_module")
@Data
public class FunctionalCaseModule implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{functional_case_module.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{functional_case_module.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_module.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID", required = true, allowableValues = "range[1, 50]")
    private String projectId;

    @Size(min = 1, max = 100, message = "{functional_case_module.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_module.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "名称", required = true, allowableValues = "range[1, 100]")
    private String name;

    @ApiModelProperty(name = "父节点ID", required = false, allowableValues = "range[1, 50]")
    private String parentId;


    @ApiModelProperty(name = "节点的层级", required = true, dataType = "Integer")
    private Integer level;

    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;

    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;

    @Size(min = 1, max = 10, message = "{functional_case_module.pos.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_module.pos.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "同一节点下的顺序", required = true, dataType = "Long")
    private Long pos;

    @Size(min = 1, max = 50, message = "{functional_case_module.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_module.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;
}
