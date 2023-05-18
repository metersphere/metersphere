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

@ApiModel(value = "误报库大字段")
@TableName("fake_error_blob")
@Data
@EqualsAndHashCode(callSuper=false)
public class FakeErrorBlob extends FakeError implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{fake_error_blob.fake_error_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "Test ID", required = true, allowableValues = "range[1, 50]")
    private String fakeErrorId;


    @ApiModelProperty(name = "内容", required = false, allowableValues = "range[1, ]")
    private byte[] content;


    @ApiModelProperty(name = "报告内容", required = false, allowableValues = "range[1, ]")
    private byte[] description;


}