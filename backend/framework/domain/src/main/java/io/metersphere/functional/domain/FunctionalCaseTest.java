package io.metersphere.functional.domain;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;

@Schema(title = "功能用例和其他用例的中间表")
@Table("functional_case_test")
@Data
public class FunctionalCaseTest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotBlank(message = "{functional_case_test.id.not_blank}", groups = {Updated.class})
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    @Size(min = 1, max = 50, message = "{functional_case_test.functional_case_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_test.functional_case_id.not_blank}", groups = {Created.class})
    @Schema(title = "功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String functionalCaseId;

    @Size(min = 1, max = 50, message = "{functional_case_test.test_id.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_test.test_id.not_blank}", groups = {Created.class})
    @Schema(title = "其他类型用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String testId;

    @Size(min = 1, max = 64, message = "{functional_case_test.test_type.length_range}", groups = {Created.class, Updated.class})
    @NotBlank(message = "{functional_case_test.test_type.not_blank}", groups = {Created.class})
    @Schema(title = "用例类型：接口用例/场景用例/性能用例/UI用例", requiredMode = Schema.RequiredMode.REQUIRED)
    private String testType;


    @Schema(title = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long createTime;


    @Schema(title = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long updateTime;


}