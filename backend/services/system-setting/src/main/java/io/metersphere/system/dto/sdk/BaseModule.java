package io.metersphere.system.dto.sdk;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseModule {
    @Schema(description = "节点id")
    private String id;

    @Schema(description = "节点名称")
    private String name;

    @Schema(description = "排序单位")
    private long pos;

    @Schema(description = "项目ID")
    private String projectId;

    @Schema(description = "父节点id")
    private String parentId;

}
