package io.metersphere.bug.domain;

import io.metersphere.validation.groups.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import lombok.Data;

@Data
public class BugFunctionalCase implements Serializable {
    @Schema(description =  "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_functional_case.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{bug_functional_case.id.length_range}", groups = {Created.class, Updated.class})
    private String id;

    @Schema(description =  "功能用例或测试计划功能用例ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_functional_case.resource_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug_functional_case.resource_id.length_range}", groups = {Created.class, Updated.class})
    private String resourceId;

    @Schema(description =  "缺陷ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_functional_case.bug_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug_functional_case.bug_id.length_range}", groups = {Created.class, Updated.class})
    private String bugId;

    @Schema(description =  "关联的类型：关联功能用例/关联测试计划功能用例", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_functional_case.ref_type.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 64, message = "{bug_functional_case.ref_type.length_range}", groups = {Created.class, Updated.class})
    private String refType;

    @Schema(description =  "测试计划的用例所指向的用例的id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{bug_functional_case.ref_id.not_blank}", groups = {Created.class})
    @Size(min = 1, max = 50, message = "{bug_functional_case.ref_id.length_range}", groups = {Created.class, Updated.class})
    private String refId;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    private static final long serialVersionUID = 1L;
}