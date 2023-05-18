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

@ApiModel(value = "功能用例的前后置关系")
@TableName("functional_case_relationship_edge")
@Data
public class FunctionalCaseRelationshipEdge implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    @NotBlank(message = "{functional_case_relationship_edge.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.source_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_relationship_edge.source_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "源节点的ID", required = true, allowableValues = "range[1, 50]")
    private String sourceId;

    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.target_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_relationship_edge.target_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "目标节点的ID", required = true, allowableValues = "range[1, 50]")
    private String targetId;

    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.graph_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_relationship_edge.graph_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "所属关系图的ID", required = true, allowableValues = "range[1, 50]")
    private String graphId;

    @Size(min = 1, max = 50, message = "{functional_case_relationship_edge.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_relationship_edge.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues = "range[1, 50]")
    private String createUser;


    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;


    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;


}