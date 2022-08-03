package io.metersphere.service;


import io.metersphere.base.domain.LoadTestReportWithBLOBs;
import io.metersphere.base.domain.Quota;
import io.metersphere.performance.request.TestPlanRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lyh
 */
public interface QuotaService {

    /**
     * api配额检查
     */
    void checkAPIDefinitionQuota(String projectId);

    /**
     * api配额检查
     */
    void checkAPIAutomationQuota(String projectId);

    /**
     * 性能测试配额检查
     * @param request 压力配置
     * @param checkPerformance 是：检查创建数量配额 / 否：检查并发数和时间
     */
    void checkLoadTestQuota(TestPlanRequest request, boolean checkPerformance);

    /**
     * 检查资源池
     * @return 资源池名称
     */
    Set<String> getQuotaResourcePools();

    /**
     * 工作空间下被限制使用的资源池
     * @param workspaceId 工作空间ID
     * @return 资源池名称Set
     */
    Set<String> getQuotaWsResourcePools(String workspaceId);

    /**
     * 检查工作空间项目数量配额
     * @param workspaceId 工作空间ID
     */
    void checkWorkspaceProject(String workspaceId);

    /**
     * 检查vumUsed配额
     * 未超过：返回本次执行预计消耗的配额
     * 超过：抛出异常
     * @param request 压力配置
     * @param projectId 性能测试所属项目ID
     * @return 本次执行预计消耗的配额
     */
    BigDecimal checkVumUsed(TestPlanRequest request, String projectId);

    /**
     * 检查向某资源添加人员时是否超额
     * @param map 资源ID:添加用户ID列表
     * @param type 检查类型 PROJECT/WORKSPACE
     */
    void checkMemberCount(Map<String, List<String>> map, String type);

    /**
     * 更新VumUsed配额
     * @param projectId 项目ID
     * @param used 预计使用数量
     */
    void updateVumUsed(String projectId, BigDecimal used);

    /**
     * 回退因主动停止或者测试启动后中途执行异常扣除的VumUsed配额
     * @param report 性能测试报告
     * @return 预计回退数量
     */
    BigDecimal getReduceVumUsed(LoadTestReportWithBLOBs report);

    /**
     * 如果有该项目配额，修改为使用工作空间下项目默认配额
     * 无该配额，新建配额并默认使用工作空间下项目默认配额
     * @return 操作后的配额
     */
    Quota projectUseDefaultQuota(String projectId);

    /**
     * 如果有该工作空间配额，修改为使用系统默认配额，
     * 无该配额，新建配额并默认使用系统配额
     * @param workspaceId 工作空间ID
     * @return 操作后的配额
     */
    Quota workspaceUseDefaultQuota(String workspaceId);

}
