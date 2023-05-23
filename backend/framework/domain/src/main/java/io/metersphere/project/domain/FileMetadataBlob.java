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

@ApiModel(value = "文件基础信息大字段")
@Table("file_metadata_blob")
@Data
@EqualsAndHashCode(callSuper=false)
public class FileMetadataBlob implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{file_metadata_blob.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "文件ID", required = true, allowableValues = "range[1, 255]")
    private String id;


    @ApiModelProperty(name = "储存库", required = false, allowableValues = "range[1, ]")
    private byte[] gitInfo;


}