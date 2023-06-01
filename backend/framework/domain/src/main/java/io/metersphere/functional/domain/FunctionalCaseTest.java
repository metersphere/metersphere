package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

@Data
public class FunctionalCaseTest implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_test.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{functional_case_test.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_test.functional_case_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_test.functional_case_id.length_range}", groups = {Created.class, Updated.class})
    private String functionalCaseId;

    @Schema(title = "其他类型用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_test.test_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{functional_case_test.test_id.length_range}", groups = {Created.class, Updated.class})
    private String testId;

    @Schema(title = "用例类型：接口用例/场景用例/性能用例/UI用例", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_case_test.test_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{functional_case_test.test_type.length_range}", groups = {Created.class, Updated.class})
    private String testType;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    private static final long serialVersionUID = 1L;
}