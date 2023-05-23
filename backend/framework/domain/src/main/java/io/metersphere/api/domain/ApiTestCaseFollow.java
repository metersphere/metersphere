
package io.metersphere.api.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@ApiModel(value = "接口用例关注人")
@Table("api_test_case_follow")
@Data
public class ApiTestCaseFollow implements Serializable {
    private static final long serialVersionUID = 1L;
    @Size(min = 1, max = 50, message = "{api_test_case_follow.case_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case_follow.case_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "用例fk", required = true, allowableValues = "range[1, 50]")
    private String caseId;

    @Size(min = 1, max = 50, message = "{api_test_case_follow.follow_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{api_test_case_follow.follow_id.not_blank}", groups = {Created.class})
    @ApiModelProperty(name = "关注人/用户fk", required = true, allowableValues = "range[1, 50]")
    private String followId;

}