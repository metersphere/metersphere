package io.metersphere.system.log.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class SystemOperationLogRequest extends BaseOperationLogRequest {

    @Schema(description =  "项目id")
    private List<String> projectIds;

    @Schema(description =  "组织id")
    private List<String> organizationIds;

}
