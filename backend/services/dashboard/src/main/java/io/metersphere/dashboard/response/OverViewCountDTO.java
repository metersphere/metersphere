package io.metersphere.dashboard.response;

import io.metersphere.dashboard.dto.NameArrayDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OverViewCountDTO {

    @Schema(description = "用例模块数量")
    private Map<String, Object> caseCountMap;

    @Schema(description = "横坐标数量")
    private List<String> xAxis;

    @Schema(description = "项目模块数量DTO")
    private List<NameArrayDTO> projectCountList;

    @Schema(description = "错误码")
    private int errorCode;

}
