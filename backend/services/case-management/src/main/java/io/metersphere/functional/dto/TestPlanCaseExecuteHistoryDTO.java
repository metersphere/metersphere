package io.metersphere.functional.dto;

import io.metersphere.plan.domain.TestPlanCaseExecuteHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class TestPlanCaseExecuteHistoryDTO extends TestPlanCaseExecuteHistory {

    @Schema(description =  "执行人头像")
    private String userLogo;

    @Schema(description =  "执行人名")
    private String userName;

    @Schema(description =  "执行人邮箱")
    private String email;

    @Schema(description =  "执行解析内容")
    private String contentText;

    @Schema(description =  "执行解析内容")
    private String stepsText;

    @Schema(description =  "测试计划名称")
    private String testPlanName;

    @Schema(description =  "测试计划id")
    private String testPlanId;

    @Schema(description =  "编辑模式")
    private String caseEditType;

    @Schema(description = "是否显示步骤信息")
    private boolean showResult = false;

}
