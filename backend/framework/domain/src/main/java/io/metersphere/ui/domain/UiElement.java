package io.metersphere.ui.domain;

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

@ApiModel(value = "元素")
@TableName("ui_element")
@Data
public class UiElement implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    @NotBlank(message = "{ui_element.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "元素id", required = true, allowableValues="range[1, 50]")
    private String id;
    
    
    @ApiModelProperty(name = "元素num", required = true, dataType = "Integer")
    private Integer num;
    
    @Size(min = 1, max = 50, message = "{ui_element.module_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element.module_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "元素所属模块id", required = true, allowableValues="range[1, 50]")
    private String moduleId;
    
    @Size(min = 1, max = 50, message = "{ui_element.project_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element.project_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "项目id", required = true, allowableValues="range[1, 50]")
    private String projectId;
    
    @Size(min = 1, max = 255, message = "{ui_element.name.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element.name.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "名称", required = true, allowableValues="range[1, 255]")
    private String name;
    
    @Size(min = 1, max = 30, message = "{ui_element.location_type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element.location_type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "定位类型", required = true, allowableValues="range[1, 30]")
    private String locationType;
    
    @Size(min = 1, max = 300, message = "{ui_element.location.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element.location.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "元素定位", required = true, allowableValues="range[1, 300]")
    private String location;
    
    @Size(min = 1, max = 100, message = "{ui_element.create_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element.create_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "创建人", required = true, allowableValues="range[1, 100]")
    private String createUser;
    
    @Size(min = 1, max = 100, message = "{ui_element.update_user.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element.update_user.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "更新人", required = true, allowableValues="range[1, 100]")
    private String updateUser;
    
    @Size(min = 1, max = 50, message = "{ui_element.version_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element.version_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "版本ID", required = true, allowableValues="range[1, 50]")
    private String versionId;
    
    @Size(min = 1, max = 50, message = "{ui_element.ref_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element.ref_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "指向初始版本ID", required = true, allowableValues="range[1, 50]")
    private String refId;
    
    
    @ApiModelProperty(name = "自定义排序，间隔5000", required = true, dataType = "Long")
    private Long pos;
    
    @Size(min = 1, max = 1, message = "{ui_element.latest.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{ui_element.latest.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "是否为最新版本 0:否，1:是", required = true, allowableValues="range[1, 1]")
    private Boolean latest;
    
    
    @ApiModelProperty(name = "元素描述", required = false, allowableValues="range[1, 1000]")
    private String description;
    
    
    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;
    
    
    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;
    

}