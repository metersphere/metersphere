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

@ApiModel(value = "误报库大字段")
@Table("fake_error_blob")
@Data
@EqualsAndHashCode(callSuper=false)
public class FakeErrorBlob implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{fake_error_blob.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "Test ID", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "内容", required = false, allowableValues = "range[1, ]")
    private byte[] content;


    @ApiModelProperty(name = "报告内容", required = false, allowableValues = "range[1, ]")
    private byte[] description;


}