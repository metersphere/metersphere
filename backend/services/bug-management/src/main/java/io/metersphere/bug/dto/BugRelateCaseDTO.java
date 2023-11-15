package io.metersphere.bug.dto;

import io.metersphere.functional.domain.FunctionalCase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BugRelateCaseDTO extends FunctionalCase {

    @Schema(description = "关联ID")
    private String relateId;

    @Schema(description = "关联类型")
    private String relateCaseType;

    @Schema(description = "是否关联计划用例")
    private Boolean relatePlanCase;

    @Schema(description = "是否关联用例")
    private Boolean relateCase;

    @Schema(description = "关联用例类型名称")
    private String relateCaseTypeName;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "版本名称")
    private String versionName;
}
