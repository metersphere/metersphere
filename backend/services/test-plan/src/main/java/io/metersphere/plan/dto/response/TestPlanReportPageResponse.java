package io.metersphere.plan.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.metersphere.system.serializer.CustomRateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TestPlanReportPageResponse {

    @Schema(description = "报告ID")
    private String id;
    @Schema(description = "报告名称")
    private String name;
    @Schema(description = "计划名称")
    private String planName;
    @Schema(description = "计划通过阈值")
    private String planPassThreshold;
    @Schema(description = "触发方式")
    private String triggerMode;
    @Schema(description = "执行状态")
    private String execStatus;
    @Schema(description = "执行结果")
    private String resultStatus;
    @Schema(description = "通过率")
    @JsonSerialize(using = CustomRateSerializer.class)
    private Double passRate;
    @Schema(description = "创建人")
    private String createUser;
    @Schema(description = "创建人名称")
    private String createUserName;
    @Schema(description = "创建时间")
    private Long createTime;
    @Schema(description = "是否是集合报告")
    private boolean integrated;

}
