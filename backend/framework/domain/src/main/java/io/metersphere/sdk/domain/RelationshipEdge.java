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
    
    /** 源节点的ID */
    @TableId
    @NotBlank(message = "源节点的ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "源节点的ID")
    private String sourceId;
    
    /** 目标节点的ID */
    @TableId
    @NotBlank(message = "目标节点的ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "目标节点的ID")
    private String targetId;
    
    /** 边的分类 */
    @Size(min = 1, max = 20, message = "边的分类长度必须在1-20之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "边的分类不能为空", groups = {Created.class})
    @ApiModelProperty(name = "边的分类")
    private String type;
    
    /** 所属关系图的ID */
    @Size(min = 1, max = 50, message = "所属关系图的ID长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "所属关系图的ID不能为空", groups = {Created.class})
    @ApiModelProperty(name = "所属关系图的ID")
    private String graphId;
    
    /** 创建人 */
    @Size(min = 1, max = 50, message = "创建人长度必须在1-50之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "创建人不能为空", groups = {Created.class})
    @ApiModelProperty(name = "创建人")
    private String createUser;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private Long createTime;
    

}