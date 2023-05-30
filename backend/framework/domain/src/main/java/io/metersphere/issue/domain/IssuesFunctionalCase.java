package io.metersphere.issue.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class IssuesFunctionalCase implements Serializable {
    @Schema(title = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{issues_functional_case.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{issues_functional_case.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(title = "功能用例或测试计划功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{issues_functional_case.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{issues_functional_case.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(title = "缺陷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{issues_functional_case.issues_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{issues_functional_case.issues_id.length_range}", groups = {Created.class, Updated.class})
    private String issuesId;

    @Schema(title = "关联的类型：关联功能用例/关联测试计划功能用例", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{issues_functional_case.ref_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{issues_functional_case.ref_type.length_range}", groups = {Created.class, Updated.class})
    private String refType;

    @Schema(title = "测试计划的用例所指向的用例的id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{issues_functional_case.ref_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{issues_functional_case.ref_id.length_range}", groups = {Created.class, Updated.class})
    private String refId;

    @Schema(title = "创建时间")
    private Long createTime;

    @Schema(title = "更新时间")
    private Long updateTime;

    private static final long serialVersionUID = 1L;
}