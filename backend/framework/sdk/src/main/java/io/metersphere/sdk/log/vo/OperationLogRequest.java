package io.metersphere.sdk.log.vo;

import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.sdk.dto.BasePageRequest;
import io.metersphere.sdk.valid.EnumValue;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class OperationLogRequest extends BasePageRequest {

    @Schema(title = "操作人")
    private String operUser;


    @Schema(title = "开始日期")
    @NotNull(message = "{start_time_is_null}")
    private Long startTime;
    @Schema(title = "结束日期")
    @NotNull(message = "{end_time_is_null}")
    private Long endTime;

    @Schema(title = "项目id")
    private List<String> projectIds;

    @Schema(title = "组织id")
    private List<String> organizationIds;

    @Schema(title = "操作类型")
    private String type;


    @Schema(title = "操作对象")
    private String module;

    @Schema(title = "名称")
    private String content;

    @Schema(title = "级别 系统|组织|项目")
    @EnumValue(enumClass = UserRoleType.class, groups = {Created.class, Updated.class})
    private String level;

}
