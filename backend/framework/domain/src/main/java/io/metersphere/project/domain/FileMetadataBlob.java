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

@ApiModel(value = "文件基础信息大字段")
@TableName("file_metadata_blob")
@Data
public class FileMetadataBlob implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 文件ID */
    @TableId
    @NotBlank(message = "文件ID不能为空", groups = {Updated.class})
    @ApiModelProperty(name = "文件ID")
    private String fileId;
    
    /** 储存库 */
    
    
    @ApiModelProperty(name = "储存库")
    private byte[] gitInfo;
    

}