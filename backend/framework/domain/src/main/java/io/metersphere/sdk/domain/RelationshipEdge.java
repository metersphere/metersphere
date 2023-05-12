package io.metersphere.sdk.domain;

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

@ApiModel(value = "关系图")
@TableName("relationship_edge")
@Data
public class RelationshipEdge implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{relationship_edge.source_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "源节点的ID", required = true, allowableValues = "range[1, 50]")
    private String sourceId;

    @TableId
    @NotBlank(message = "{relationship_edge.target_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "目标节点的ID", required = true, allowableValues = "range[1, 50]")
    private String targetId;

    @Size(min = 1, max = 20, message = "{relationship_edge.type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{relationship_edge.type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "边的分类", required = true, allowableValues = "range[1, 20]")
    private String type;

    @Size(min = 1, max = 50, message = "{relationship_edge.graph_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{relationship_edge.graph_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "所属关系图的ID", required = true, allowableValues = "range[1, 50]")
    private String graphId;

    @Size(min = 1, max = 50, message = "{relationship_edge.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{relationship_edge.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;


    @ApiModelProperty(name = "", required = true, allowableValues = "range[1, ]")
    private Long createTime;


}