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
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@ApiModel(value = "场景大字段")
@TableName("ui_scenario_blob")
@Data
@EqualsAndHashCode(callSuper=false)
public class UiScenarioBlob implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    @NotBlank(message = "{ui_scenario_blob.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "场景ID", required = true, allowableValues = "range[1, 50]")
    private String id;


    @ApiModelProperty(name = "场景定义", required = false, allowableValues = "range[1, ]")
    private byte[] scenarioDefinition;


    @ApiModelProperty(name = "描述", required = false, allowableValues = "range[1, ]")
    private byte[] description;


    @ApiModelProperty(name = "环境", required = false, allowableValues = "range[1, ]")
    private byte[] environmentJson;


}