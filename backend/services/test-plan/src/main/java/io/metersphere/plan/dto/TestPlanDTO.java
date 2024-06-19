package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


/**
 * @author wx
 */
@Data
@NoArgsConstructor
public class TestPlanDTO {
    private String id;

    @Schema(description = "message.domain.test_plan_num")
    private Long num;

    @Schema(description = "message.domain.test_plan_name")
    private String name;

    @Schema(description = "message.domain.test_plan_status")
    private String status;

    @Schema(description = "message.domain.test_plan_type")
    private String type;

    @Schema(description = "message.domain.test_plan_tags")
    private java.util.List<String> tags;

    @Schema(description = "message.domain.test_plan_createTime")
    private Long createTime;

    @Schema(description = "message.domain.test_plan_createUser")
    private String createUser;

    @Schema(description = "message.domain.test_plan_updateTime")
    private Long updateTime;

    @Schema(description = "message.domain.test_plan_updateUser")
    private String updateUser;

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

    private List<String> followUsers;

}
