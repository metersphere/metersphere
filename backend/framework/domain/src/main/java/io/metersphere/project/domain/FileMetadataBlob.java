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
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@ApiModel(value = "文件基础信息大字段")
@TableName("file_metadata_blob")
@Data
@EqualsAndHashCode(callSuper=false)
public class FileMetadataBlob extends FileMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{file_metadata_blob.file_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "文件ID", required = true, allowableValues = "range[1, 255]")
    private String fileId;


    @ApiModelProperty(name = "储存库", required = false, allowableValues = "range[1, ]")
    private byte[] gitInfo;


}