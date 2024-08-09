package io.metersphere.plan.dto.response;

import io.metersphere.plan.domain.TestPlanCaseExecuteHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author wx
 */
@Data
public class TestPlanCaseExecHistoryResponse extends TestPlanCaseExecuteHistory {

    @Schema(description = "评审解析内容")
    private String contentText;

    @Schema(description = "步骤结果")
    private String stepsExecResult;

    @Schema(description = "执行人姓名")
    private String userName;

    @Schema(description = "执行人头像")
    private String userLogo;

    @Schema(description = "执行人邮箱")
    private String email;

    @Schema(description = "步骤描述")
    private boolean stepModule = false;
}
