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
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@ApiModel(value = "缺陷模板扩展")
@Table("issue_template_extend")
@Data
@EqualsAndHashCode(callSuper = false)
public class IssueTemplateExtend implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{issue_template_extend.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "缺陷模板ID", required = true, allowableValues = "range[1, 100]")
    private String id;


    @ApiModelProperty(name = "缺陷标题模板", required = false, allowableValues = "range[1, 64]")
    private String title;


    @ApiModelProperty(name = "缺陷内容模板", required = false, allowableValues = "range[1, ]")
    private String content;


}