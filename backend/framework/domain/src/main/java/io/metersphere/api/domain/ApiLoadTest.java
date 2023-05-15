
package io.metersphere.api.domain;

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

@ApiModel(value = "关联场景测试和性能测试")
@TableName("api_load_test")
@Data
public class ApiLoadTest implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    @NotBlank(message = "{api_load_test.id.not_blank}", groups = {Updated.class})
    @ApiModelProperty(name = "ID", required = true, allowableValues = "range[1, 50]")
    private String id;

    @Size(min = 1, max = 50, message = "{api_load_test.resource_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_load_test.resource_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "接口用例fk/场景用例fk", required = true, allowableValues = "range[1, 50]")
    private String resourceId;

    @Size(min = 1, max = 50, message = "{api_load_test.load_test_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_load_test.load_test_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "性能测试用例fk", required = true, allowableValues = "range[1, 50]")
    private String loadTestId;

    @ApiModelProperty(name = "环境fk", required = false, allowableValues = "range[1, 50]")
    private String environmentId;

    @Size(min = 1, max = 10, message = "{api_load_test.type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_load_test.type.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "资源类型/CASE/SCENARIO", required = true, allowableValues = "range[1, 10]")
    private String type;


}