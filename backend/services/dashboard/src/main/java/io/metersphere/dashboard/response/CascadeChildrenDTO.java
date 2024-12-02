package io.metersphere.dashboard.response;


import io.metersphere.dashboard.dto.CascadeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CascadeChildrenDTO {

    @Schema(description = "值/id")
    private String value;

    @Schema(description = "名称/标签")
    private String label;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "关联子集")
    private List<CascadeDTO> children;

}
