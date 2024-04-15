package io.metersphere.system.log.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class OrgOperationLogRequest extends BaseOperationLogRequest {

    @Schema(description = "项目id")
    private List<String> projectIds;

}
