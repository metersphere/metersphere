package io.metersphere.functional.domain;

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

@ApiModel(value = "功能用例和其他用例的中间表")
@TableName("functional_case_test")
@Data
public class FunctionalCaseTest implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{functional_case_test.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{functional_case_test.functional_case_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_test.functional_case_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "功能用例ID", required = true, allowableValues = "range[1, 50]")
    private String functionalCaseId;

    @Size(min = 1, max = 50, message = "{functional_case_test.test_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_test.test_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "其他类型用例ID", required = true, allowableValues = "range[1, 50]")
    private String testId;

    @Size(min = 1, max = 64, message = "{functional_case_test.test_type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_test.test_type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "用例类型：接口用例/场景用例/性能用例/UI用例", required = true, allowableValues = "range[1, 64]")
    private String testType;


    @ApiModelProperty(name = "创建时间", required = true, dataType = "Long")
    private Long createTime;


    @ApiModelProperty(name = "更新时间", required = true, dataType = "Long")
    private Long updateTime;


}