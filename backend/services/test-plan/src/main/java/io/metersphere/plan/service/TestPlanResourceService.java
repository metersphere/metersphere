package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.dto.ResourceLogInsertModule;
import io.metersphere.plan.dto.TestPlanResourceAssociationParam;
import io.metersphere.plan.dto.request.BasePlanCaseBatchRequest;
import io.metersphere.plan.dto.response.TestPlanAssociationResponse;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.system.dto.LogInsertModule;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

//测试计划关联表 通用方法
@Transactional(rollbackFor = Exception.class)
public abstract class TestPlanResourceService extends TestPlanSortService {

    public abstract void deleteBatchByTestPlanId(List<String> testPlanIdList);

    public abstract Map<String, Long> caseExecResultCount(String testPlanId);

    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanResourceLogService testPlanResourceLogService;

    /**
     * 取消关联资源od
     *
     * @return TestPlanAssociationResponse
     */
    public TestPlanAssociationResponse disassociate(
            String resourceType,
            BasePlanCaseBatchRequest request,
            @Validated LogInsertModule logInsertModule,
            List<String> associationIdList,
            Consumer<TestPlanResourceAssociationParam> disassociate) {
        TestPlanAssociationResponse response = new TestPlanAssociationResponse();
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getTestPlanId());
        //获取有效ID
        if (CollectionUtils.isNotEmpty(associationIdList)) {
            TestPlanResourceAssociationParam associationParam = new TestPlanResourceAssociationParam(associationIdList, testPlan.getProjectId(), testPlan.getId(), testPlan.getNum(), logInsertModule.getOperator());
            disassociate.accept(associationParam);
            response.setAssociationCount(associationIdList.size());
            testPlanResourceLogService.saveDisassociateLog(testPlan, new ResourceLogInsertModule(resourceType, logInsertModule));
        }
        return response;
    }

    public abstract long copyResource(String originalTestPlanId, String newTestPlanId, String operator, long operatorTime);
}
