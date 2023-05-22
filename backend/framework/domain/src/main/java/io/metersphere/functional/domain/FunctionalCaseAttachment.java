package io.metersphere.functional.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "功能用例和附件的中间表")
@TableName("functional_case_attachment")
@Data
public class FunctionalCaseAttachment implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "{functional_case_attachment.functional_case_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "功能用例ID", required = true, allowableValues = "range[1, 50]")
    private String functionalCaseId;

    @NotBlank(message = "{functional_case_attachment.file_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "文件的ID", required = true, allowableValues = "range[1, 50]")
    private String fileId;

}
