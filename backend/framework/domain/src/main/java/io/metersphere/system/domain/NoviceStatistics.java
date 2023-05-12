package io.metersphere.system.domain;

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

@ApiModel(value = "新手村")
@TableName("novice_statistics")
@Data
public class NoviceStatistics implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**  */
    @TableId
    @NotBlank(message = "不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "")
    private String id;
    
    /** 用户id */
    
    
    @ApiModelProperty(name = "用户id")
    private String userId;
    
    /** 新手引导完成的步骤 */
    @Size(min = 1, max = 1, message = "新手引导完成的步骤长度必须在1-1之间", groups = {Created.class, Updated.class})
    @NotBlank(message = "新手引导完成的步骤不能为空", groups = {Created.class})
    @ApiModelProperty(name = "新手引导完成的步骤")
    private Boolean guideStep;
    
    /** 新手引导的次数 */
    
    
    @ApiModelProperty(name = "新手引导的次数")
    private Integer guideNum;
    
    /** data option (JSON format) */
    
    
    @ApiModelProperty(name = "data option (JSON format)")
    private byte[] dataOption;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private Long createTime;
    
    /**  */
    
    
    @ApiModelProperty(name = "")
    private Long updateTime;
    

}