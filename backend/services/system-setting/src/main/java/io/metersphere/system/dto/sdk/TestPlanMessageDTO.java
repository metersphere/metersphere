package io.metersphere.system.dto.sdk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestPlanMessageDTO {
    @Schema(description = "message.domain.test_plan_num")
    private String num;

    @Schema(description = "message.domain.test_plan_name")
    private String name;

    @Schema(description = "message.domain.test_plan_status")
    private String status;

    @Schema(description = "message.domain.api_scenario_status")
    private String type;

    @Schema(description = "message.domain.test_plan_tags")
    private List<String> tags;

    @Schema(description = "message.domain.test_plan_createUser")
    private String createUser;

    @Schema(description = "message.domain.test_plan_createTime")
    private Long createTime;

    @Schema(description = "message.domain.test_plan_updateUser")
    private String updateUser;

    @Schema(description = "message.domain.test_plan_updateTime")
    private Long updateTime;

    @Schema(description = "message.domain.test_plan_plannedStartTime")
    private Long plannedStartTime;

    @Schema(description = "message.domain.test_plan_plannedEndTime")
    private Long plannedEndTime;

    @Schema(description = "message.domain.test_plan_actualStartTime")
    private Long actualStartTime;

    @Schema(description = "message.domain.test_plan_actualEndTime")
    private Long actualEndTime;

    @Schema(description = "message.domain.test_plan_description")
    private String description;

    @Schema(description = "message.domain.test_plan_reportName")
    private String reportName;

    @Schema(description = "message.domain.test_plan_reportUrl")
    private String reportUrl;

    @Schema(description = "message.domain.test_plan_reportShareUrl")
    private String reportShareUrl;

    @Schema(description = "message.domain.test_plan_startTime")
    private Long startTime;

    @Schema(description = "message.domain.test_plan_endTime")
    private Long endTime;

    @Schema(description = "message.domain.test_plan_execStatus")
    private String execStatus;

    @Schema(description = "message.domain.test_plan_resultStatus")
    private String resultStatus;

    @Schema(description = "message.domain.test_plan_passRate")
    private Double passRate;

    @Schema(description = "message.domain.test_plan_passThreshold")
    private Double passThreshold;

    @Schema(description = "message.domain.test_plan_executeRate")
    private Double executeRate;

}
