package io.metersphere.functional.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class FunctionalThirdDemandPageRequest {


    @Schema(description = "ms系统当前的项目id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{functional_third_demand_page_request.project_id.not_blank}")
    private String projectId;

    @Schema(description = "需求分页查询关键字")
    private String query;

    @Schema(description = "列表筛选条件")
    private Map<String, Object> filter;

    @Schema(description = "开始页码", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "当前页码必须大于0")
    private int startPage;

    @Schema(description = "每页条数", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 5, message = "每页显示条数必须不小于5")
    @Max(value = 500, message = "每页显示条数不能大于500")
    private int pageSize;
}
