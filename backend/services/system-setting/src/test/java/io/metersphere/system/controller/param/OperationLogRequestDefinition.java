package io.metersphere.system.controller.param;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OperationLogRequestDefinition {


    private String operUser;


    @NotNull(message = "{start_time_is_null}")
    private Long startTime;
    @NotNull(message = "{end_time_is_null}")
    private Long endTime;

    private List<String> projectIds;

    private List<String> organizationIds;

    private String type;


    private String module;

    private String content;

    private String level;

}
