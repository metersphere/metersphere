package io.metersphere.system.log.vo;

import io.metersphere.sdk.constants.UserRoleType;
import io.metersphere.system.dto.sdk.BasePageRequest;
import io.metersphere.sdk.valid.EnumValue;
import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BaseOperationLogRequest extends BasePageRequest {

    @Schema(description = "操作人")
    private String operUser;

    @Schema(description = "开始日期")
    @NotNull(message = "{start_time_is_null}")
    private Long startTime;
    @Schema(description = "结束日期")
    @NotNull(message = "{end_time_is_null}")
    private Long endTime;

    @Schema(description = "操作类型")
    private String type;


    @Schema(description = "操作对象")
    private String module;

    @Schema(description = "名称")
    private String content;

    @Schema(description = "级别 系统|组织|项目")
    @EnumValue(enumClass = UserRoleType.class, groups = {Created.class, Updated.class})
    private String level;

}
