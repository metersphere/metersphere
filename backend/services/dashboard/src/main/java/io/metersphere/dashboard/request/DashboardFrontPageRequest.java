package io.metersphere.dashboard.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class DashboardFrontPageRequest extends DashboardBaseRequest{

    @Schema(description = "项目ID集合")
    private List<String> projectIds;

    @Schema(description = "人员集合")
    private List<String> handleUsers;
}
