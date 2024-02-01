package io.metersphere.api.dto.scenario;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ApiScenarioAssociationDTO {
    @Schema(description = "编号")
    private String resourceNum;
    @Schema(description = "资源id")
    private String resourceId;
    @Schema(description = "资源名称")
    private String name;
    @Schema(description = "资源类型 ")
    private String stepType;
    @Schema(description = "引用类型 COPY:复制，REF:引用")
    private String refType;
    @Schema(description = "项目名称")
    private String projectName;
    @Schema(description = "项目id")
    private String projectId;


}
