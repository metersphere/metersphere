package io.metersphere.functional.dto;

import io.metersphere.functional.domain.FunctionalCaseTest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class FunctionalCaseTestDTO extends FunctionalCaseTest {

    @Schema(description = "用例所属项目名称")
    private String projectName;

    @Schema(description = "用例所在版本名称")
    private String versionName;

    @Schema(description = "用例名称")
    private String sourceName;

    @Schema(description = "用例Id")
    private String sourceNum;
}
