package io.metersphere.load.domain;

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

@ApiModel(value = "性能测试用例大字段")
@TableName("load_test_blob")
@Data
@EqualsAndHashCode(callSuper=false)
public class LoadTestBlob implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId
    @NotBlank(message = "{load_test_blob.test_id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "测试ID", required = true, allowableValues = "range[1, 50]")
    private String testId;


    @ApiModelProperty(name = "压力配置", required = false, allowableValues = "range[1, ]")
    private byte[] loadConfiguration;


    @ApiModelProperty(name = "高级配置", required = false, allowableValues = "range[1, ]")
    private byte[] advancedConfiguration;


    @ApiModelProperty(name = "环境信息 (JSON format)", required = false, allowableValues = "range[1, ]")
    private byte[] envInfo;


}