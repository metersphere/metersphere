package io.metersphere.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * @author guoyuqi
 */
@Data
public class TestPlanCollectionMinderEditDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "节点ID(新增的时候前端传UUid，更新的时候必填)")
    private String id;

    @Schema(description = "是否临时节点 {新增节点时, 传入true}")
    private Boolean tempCollectionNode = false;

    @Schema(description = "节点名称")
    @NotBlank(message = "{test_plan_collection.name.not_blank}")
    private String text;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_collection.num.not_blank}")
    private Long num;

    @Schema(description = "串并行值(串：SERIAL/并：PARALLEL)")
    @NotBlank(message = "{test_plan_collection.execute_method.not_blank}")
    private String executeMethod;

    @Schema(description = "测试集类型(功能：FUNCTIONAL/接口用例：API/场景：SCENARIO)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_collection_minder_edit.collection_type.not_blank}")
    private String type;

    @Schema(description = "是否继承", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_collection.extended.not_blank}")
    private Boolean extended;

    @Schema(description = "是否使用环境组", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_collection.grouped.not_blank}")
    private Boolean grouped = false;

    @Schema(description = "环境ID/环境组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_collection.environment_id.not_blank}")
    private String environmentId;

    @Schema(description = "测试资源池ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_collection.test_resource_pool_id.not_blank}")
    private String testResourcePoolId;

    @Schema(description = "是否失败重试", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_collection.retry_on_fail.not_blank}")
    private Boolean retryOnFail;

    @Schema(description = "失败重试类型(步骤:STEP/场景:SCENARIO)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_collection.retry_type.not_blank}")
    private String retryType;

    @Schema(description = "失败重试次数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_collection.retry_times.not_blank}")
    private Integer retryTimes;

    @Schema(description = "失败重试间隔(单位: ms)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_collection.retry_interval.not_blank}")
    private Integer retryInterval;

    @Schema(description = "是否失败停止", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{test_plan_collection.stop_on_fail.not_blank}")
    private Boolean stopOnFail;

    @Schema(description = "叶子等级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{test_plan_collection.num.not_blank}")
    private Integer level;

    @Schema(description = "关联关系的数据")
    private List<TestPlanCollectionAssociateDTO> associateDTOS;

}
