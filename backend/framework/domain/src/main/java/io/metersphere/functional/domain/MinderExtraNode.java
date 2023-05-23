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

@ApiModel(value = "脑图临时节点")
@Table("minder_extra_node")
@Data
public class MinderExtraNode implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @NotBlank(message = "{minder_extra_node.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues="range[1, 50]")
    private String id;
    
    @Size(min = 1, max = 50, message = "{minder_extra_node.parent_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{minder_extra_node.parent_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "父节点的ID，即模块ID", required = true, allowableValues="range[1, 50]")
    private String parentId;
    
    @Size(min = 1, max = 50, message = "{minder_extra_node.group_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{minder_extra_node.group_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目ID，可扩展为其他资源ID", required = true, allowableValues="range[1, 50]")
    private String groupId;
    
    @Size(min = 1, max = 30, message = "{minder_extra_node.type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{minder_extra_node.type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "类型，如：用例编辑脑图", required = true, allowableValues="range[1, 30]")
    private String type;
    
    
    @ApiModelProperty(name = "存储脑图节点额外信息", required = true, allowableValues="range[1, ]")
    private String nodeData;
    

}