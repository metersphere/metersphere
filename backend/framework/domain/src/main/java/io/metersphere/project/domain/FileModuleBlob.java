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

@ApiModel(value = "文件管理模块大字段")
@TableName("file_module_blob")
@Data
public class FileModuleBlob implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** ID */
    @TableId
    @NotBlank(message = "ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "ID")
    private String fileModuleId;
    
    /** 存储库描述 */
    
    
    @ApiModelProperty(name = "存储库描述")
    private byte[] repositoryDesc;
    
    /** 存储库路径 */
    
    
    @ApiModelProperty(name = "存储库路径")
    private String repositoryPath;
    
    /** 存储库Token */
    
    
    @ApiModelProperty(name = "存储库Token")
    private String repositoryUserName;
    
    /** 存储库Token */
    
    
    @ApiModelProperty(name = "存储库Token")
    private String repositoryToken;
    

}