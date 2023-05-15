
package io.metersphere.api.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

@ApiModel(value = "场景步骤详情")
@TableName("api_scenario_blob")
@Data
public class ApiScenarioBlob implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_scenario_blob.api_scenario_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "场景pk", required = true, allowableValues = "range[1, 50]")
    private String apiScenarioId;

    @ApiModelProperty(name = "场景步骤内容", required = false, dataType = "byte[]")
    private byte[] content;

}