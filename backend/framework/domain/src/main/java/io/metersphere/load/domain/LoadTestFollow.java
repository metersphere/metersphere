package io.metersphere.load.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@ApiModel(value = "性能用例关注人")
@Table("load_test_follow")
@Data
public class LoadTestFollow implements Serializable {
    private static final long serialVersionUID = 1L;
    @Size(min = 1, max = 50, message = "{load_test_follow.test_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_follow.test_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "测试ID", required = true, allowableValues = "range[1, 50]")
    private String testId;

    @Size(min = 1, max = 50, message = "{load_test_follow.follow_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{load_test_follow.follow_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "关注人ID", required = true, allowableValues = "range[1, 50]")
    private String followId;


}