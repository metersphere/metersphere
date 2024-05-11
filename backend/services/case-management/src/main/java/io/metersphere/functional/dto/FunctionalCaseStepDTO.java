package io.metersphere.functional.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FunctionalCaseStepDTO {

    @Schema(description = "步骤id")
    private String id;

    @Schema(description = "步骤序号")
    private Integer num;

    @Schema(description = "步骤描述")
    private String desc;

    @Schema(description = "步骤结果")
    private String result;

    @Schema(description = "实际结果")
    private String actualResult;

    @Schema(description = "执行结果（失败/成功/阻塞）")
    private String executeResult;

}
