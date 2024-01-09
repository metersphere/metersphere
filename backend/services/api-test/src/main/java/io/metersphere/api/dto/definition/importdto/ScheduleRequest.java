package io.metersphere.api.dto.definition.importdto;


import io.metersphere.api.dto.request.http.Header;
import io.metersphere.api.dto.request.http.QueryParam;
import io.metersphere.api.dto.request.http.auth.HTTPAuth;
import io.metersphere.system.domain.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ScheduleRequest extends Schedule {

    //定时任务来源： 测试计划/测试场景
    private String scheduleFrom;

    private String projectId;

    private String moduleId;

    private String modulePath;

    private String modeId;

    private String swaggerUrl;

    private String taskId;

    // 鉴权相关
    @Schema(description = "swagger的请求头参数")
    private List<Header> headers;
    @Schema(description = "swagger的请求参数")
    private List<QueryParam> arguments;
    @Schema(description = "swagger的认证参数")
    private HTTPAuth authManager;
    private Boolean coverModule = false;

}
