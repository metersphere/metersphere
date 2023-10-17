package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FunctionalCaseMessageDTO {

    @Schema(description =  "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description =  "测试计划名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String testPlanName;

    @Schema(description =  "评审名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reviewName;

    @Schema(description =  "评审状态：未开始/进行中/已完成/已结束", requiredMode = Schema.RequiredMode.REQUIRED)
    private String reviewStatus;

    @Schema(description =  "编辑模式：步骤模式/文本模式", requiredMode = Schema.RequiredMode.REQUIRED)
    private String caseModel;

    @Schema(description =  "最近的执行结果：未执行/通过/失败/阻塞/跳过", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastExecuteResult;

    @Schema(description =  "创建人")
    private String createUser;

    @Schema(description =  "更新人")
    private String updateUser;

    @Schema(description =  "删除人")
    private String deleteUser;

    @Schema(description =  "创建时间")
    private Long createTime;

    @Schema(description =  "更新时间")
    private Long updateTime;

    @Schema(description =  "删除时间")
    private Long deleteTime;

}
