package io.metersphere.bug.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BugRelateCaseDTO{

    @Schema(description = "关联ID")
    private String relateId;

    @Schema(description = "关联用例ID")
    private String relateCaseId;

    @Schema(description = "关联用例业务ID")
    private String relateCaseNum;

    @Schema(description = "关联用例名称")
    private String relateCaseName;

    @Schema(description = "关联类型")
    private String relateCaseType;

    @Schema(description = "是否通过计划关联")
    private Boolean relatePlanCase;

    @Schema(description = "关联用例类型名称")
    private String relateCaseTypeName;

    @Schema(description = "项目名称")
    private String projectName;

    @Schema(description = "版本名称")
    private String versionName;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "版本ID")
    private String versionId;
}
