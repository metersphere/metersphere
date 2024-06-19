package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanCollectionExample;
import io.metersphere.plan.dto.ResourceLogInsertModule;
import io.metersphere.plan.dto.TestPlanCollectionDTO;
import io.metersphere.plan.dto.TestPlanResourceAssociationParam;
import io.metersphere.plan.dto.request.BaseCollectionAssociateRequest;
import io.metersphere.plan.dto.request.BasePlanCaseBatchRequest;
import io.metersphere.plan.dto.response.TestPlanAssociationResponse;
import io.metersphere.plan.mapper.TestPlanCollectionMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.dto.sdk.SessionUser;
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
    @Resource
    private TestPlanCollectionMapper testPlanCollectionMapper;

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

    public abstract long copyResource(String originalTestPlanId, String newTestPlanId, Map<String, String> oldCollectionIdToNewCollectionId, String operator, long operatorTime);

    /**
     * 关联用例
     *
     * @param planId               计划ID
     * @param collectionAssociates 测试集关联用例参数
     */
    public abstract void associateCollection(String planId, Map<String, List<BaseCollectionAssociateRequest>> collectionAssociates, SessionUser user);

    /**
     * 初始化旧的关联资源到默认测试集
     *
     * @param planId
     * @param defaultCollections 默认的测试集集合
     */
    public abstract void initResourceDefaultCollection(String planId, List<TestPlanCollectionDTO> defaultCollections);


    /**
     * 校验测试集是否存在
     *
     * @param testPlanId
     * @param collectionId
     * @param type
     */
    public void checkCollection(String testPlanId, String collectionId, String type) {
        TestPlanCollectionExample collectionExample = new TestPlanCollectionExample();
        collectionExample.createCriteria().andIdEqualTo(collectionId).andParentIdNotEqualTo(ModuleConstants.ROOT_NODE_PARENT_ID).andTestPlanIdEqualTo(testPlanId).andTypeEqualTo(type);
        if (testPlanCollectionMapper.countByExample(collectionExample) == 0) {
            throw new MSException(Translator.get("test_plan.collection_not_exist"));
        }
    }
}
