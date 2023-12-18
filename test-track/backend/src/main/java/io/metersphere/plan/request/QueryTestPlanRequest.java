package io.metersphere.plan.request;

import io.metersphere.base.domain.TestPlan;
import io.metersphere.request.OrderRequest;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class QueryTestPlanRequest extends TestPlan {

    @Serial
    private static final long serialVersionUID = -9022330526265056106L;

    private boolean recent = false;

    private List<String> planIds;

    private String scenarioId;

    private String apiId;

    private String loadId;

    private List<OrderRequest> orders;

    private Map<String, List<String>> filters;

    private Map<String, Object> combine;

    private String projectId;

    private String projectName;

    /**
     * 执行人或者负责人
     */
    private String executorOrPrincipal;

    /**
     * 是否通过筛选条件查询（这个字段针对我的工作台-页面列表上的筛选做特殊处理）
     */
    private boolean byFilter;

    private List<String> filterStatus;

    /**
     * @since 2.10.10 添加模块树条件, 批量移动条件
     */
    private List<String> nodeIds;
    private Boolean selectAll;
    private List<String> unSelectIds;
}
