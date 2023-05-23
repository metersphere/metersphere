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

@ApiModel(value = "文件管理模块大字段")
@Table("file_module_blob")
@Data
@EqualsAndHashCode(callSuper=false)
public class FileModuleBlob implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{file_module_blob.file_module_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "存储库描述", required = false, allowableValues = "range[1, ]")
    private byte[] repositoryDesc;


    @ApiModelProperty(name = "存储库路径", required = false, allowableValues = "range[1, 255]")
    private String repositoryPath;


    @ApiModelProperty(name = "存储库Token", required = false, allowableValues = "range[1, 255]")
    private String repositoryUserName;


    @ApiModelProperty(name = "存储库Token", required = false, allowableValues = "range[1, 255]")
    private String repositoryToken;


}