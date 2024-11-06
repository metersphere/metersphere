package io.metersphere.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LayoutDTO {
    @Schema(description =  "布局卡片key")
    private String id;
    @Schema(description =  "排序")
    private Integer pos;
    @Schema(description =  "全屏/半屏")
    private Boolean fullScreen;
    @Schema(description =  "选中的项目ID")
    private List<String> projectIds;

}
