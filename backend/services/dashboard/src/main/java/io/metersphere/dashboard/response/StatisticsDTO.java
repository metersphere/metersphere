package io.metersphere.dashboard.response;

import io.metersphere.dashboard.dto.NameCountDTO;
import io.metersphere.dashboard.dto.StatusPercentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsDTO {

    @Schema(description = "指标DTO")
    private Map<String, List<NameCountDTO>> statusStatisticsMap;

    @Schema(description = "百分比集合")
    private List<StatusPercentDTO>statusPercentList;

    @Schema(description = "错误码")
    private int errorCode;
}
