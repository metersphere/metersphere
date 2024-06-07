package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.dto.ResourceLogInsertModule;
import io.metersphere.plan.dto.TestPlanResourceAssociationParam;
import io.metersphere.plan.dto.request.BaseAssociateCaseRequest;
import io.metersphere.plan.dto.request.TestPlanAssociationRequest;
import io.metersphere.plan.dto.request.TestPlanBatchProcessRequest;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.TestPlanResourceConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.TestPlanModuleExample;
import io.metersphere.system.dto.LogInsertModule;
import io.metersphere.system.mapper.TestPlanModuleMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanBaseUtilsService {

    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private TestPlanModuleMapper testPlanModuleMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private TestPlanCaseService testPlanCaseService;
    @Resource
    private TestPlanResourceLogService testPlanResourceLogService;

    /**
     * 校验模块
     *
     * @param moduleId
     */
    public void checkModule(String moduleId) {
        if (!StringUtils.equals(moduleId, ModuleConstants.DEFAULT_NODE_ID)) {
            TestPlanModuleExample example = new TestPlanModuleExample();
            example.createCriteria().andIdEqualTo(moduleId);
            if (testPlanModuleMapper.countByExample(example) == 0) {
                throw new MSException(Translator.get("module.not.exist"));
            }
        }
    }

    /**
     * 获取选择的计划id集合
     *
     * @param request
     * @return
     */
    public List<String> getSelectIds(TestPlanBatchProcessRequest request) {
        if (request.isSelectAll()) {
            List<String> ids = extTestPlanMapper.selectIdByConditions(request);
            if (org.apache.commons.collections.CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                ids.removeAll(request.getExcludeIds());
            }
            return ids;
        } else {
            return request.getSelectIds();
        }
    }

    /**
     * 关联用例
     *
     * @param request
     */
    //TODO 后续删除此方法以及调用controlle   后续改成通过计划集保存用例
    public void association(TestPlanAssociationRequest request, String operator) {
        TestPlan testPlan = testPlanMapper.selectByPrimaryKey(request.getTestPlanId());
        handleAssociateCase(request, operator, testPlan);
    }

    /**
     * 处理关联的用例
     *
     * @param request
     * @return
     */
    public void handleAssociateCase(BaseAssociateCaseRequest request, String operator, TestPlan testPlan) {
        //关联的功能用例
        handleFunctionalCase(request.getFunctionalSelectIds(), operator, testPlan);
        //TODO 关联接口用例/接口场景用例 handleApi(request.getApiSelectIds(),request.getApiCaseSelectIds())

    }

    /**
     * 关联的功能用例
     *
     * @param functionalSelectIds
     */
    private void handleFunctionalCase(List<String> functionalSelectIds, String operator, TestPlan testPlan) {
        if (CollectionUtils.isNotEmpty(functionalSelectIds)) {
            TestPlanResourceAssociationParam associationParam = new TestPlanResourceAssociationParam(functionalSelectIds, testPlan.getProjectId(), testPlan.getId(), testPlan.getNum(), testPlan.getCreateUser());
            testPlanCaseService.saveTestPlanResource(associationParam);
            testPlanResourceLogService.saveAssociateLog(testPlan, new ResourceLogInsertModule(TestPlanResourceConstants.RESOURCE_FUNCTIONAL_CASE, new LogInsertModule(operator, "/test-plan/association", HttpMethodConstants.POST.name())));
        }
    }
}
