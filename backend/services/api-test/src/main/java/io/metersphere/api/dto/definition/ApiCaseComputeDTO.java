package io.metersphere.api.dto.definition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: LAN
 * @date: 2023/11/8 19:17
 * @version: 1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApiCaseComputeDTO {

    @Schema(description = "更新人名称")
    private String updateUserName;

    @Schema(description = "删除人名称")
    private String deleteUserName;

    @Schema(description = "版本名称")
    private String versionName;

    @Schema(description = "接口ID")
    private String apiDefinitionId;

    @Schema(description = "用例数")
    private int caseTotal;

    @Schema(description = "用例执行结果")
    private String caseStatus;

    @Schema(description = "用例通过率")
    private String casePassRate;

    @Schema(description = "用例成功")
    private int success;

    @Schema(description = "用例失败")
    private int error;

}
