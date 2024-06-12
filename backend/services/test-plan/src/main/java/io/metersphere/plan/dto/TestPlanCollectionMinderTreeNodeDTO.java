package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TestPlanCollectionMinderTreeNodeDTO {

    @Schema(description = "节点ID")
    private String id;

    @Schema(description = "节点顺序")
    private Long pos;

    @Schema(description = "节点名称")
    private String text;

    @Schema(description = "数量")
    private int num;

    @Schema(description = "串并行")
    private Integer priority;

    @Schema(description = "串并行值")
    private String executeMethod;

    @Schema(description = "节点标签")
    private List<String> resource;

    @Schema(description = "节点状态")
    private String expandState = "expand";

    @Schema(description = "测试集类型(功能/接口/场景)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @Schema(description = "是否继承", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean extended;

    @Schema(description = "是否使用环境组", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean grouped = false;

    @Schema(description = "环境ID/环境组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String environmentId;

    @Schema(description = "测试资源池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String testResourcePoolId;

    @Schema(description = "是否失败重试", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean retryOnFail;

    @Schema(description = "失败重试类型(步骤/场景)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String retryType;

    @Schema(description = "失败重试次数", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer retryTimes;

    @Schema(description = "失败重试间隔(单位: ms)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer retryInterval;

    @Schema(description = "是否失败停止", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean stopOnFail;

}
