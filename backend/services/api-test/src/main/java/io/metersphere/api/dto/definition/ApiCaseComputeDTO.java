package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author: LAN
 * @date: 2023/11/8 19:17
 * @version: 1.0
 */
@Data
public class ApiCaseComputeDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "接口ID")
    private String apiDefinitionId;

    @Schema(description = "用例数")
    private int caseTotal;

    @Schema(description = "用例执行结果")
    private String caseStatus;

    @Schema(description = "用例通过率")
    private String casePassRate;

    @Schema(description = "成功用例")
    private int success;

    @Schema(description = "失败用例")
    private int error;

    @Schema(description = "误报用例")
    private int fakeError;

}
