package io.metersphere.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LayoutDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description =  "布局卡片id")
    private String id;
    @Schema(description =  "布局卡片key")
    private String key;
    @Schema(description =  "布局卡片label")
    private String label;
    @Schema(description =  "排序")
    private Integer pos;
    @Schema(description =  "全屏/半屏")
    private Boolean fullScreen;
    @Schema(description =  "选中的项目ID")
    private List<String> projectIds;
    @Schema(description = "是否全选")
    private boolean selectAll;
    @Schema(description =  "人员集合")
    private List<String> handleUsers;
    @Schema(description = "测试计划ID")
    private String planId;
    @Schema(description = "测试计划组ID")
    private String groupId;

}
