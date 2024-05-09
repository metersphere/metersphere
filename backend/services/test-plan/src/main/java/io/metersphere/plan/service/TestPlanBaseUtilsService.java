package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanExample;
import io.metersphere.plan.dto.request.TestPlanBatchProcessRequest;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.TestPlanModuleExample;
import io.metersphere.system.mapper.TestPlanModuleMapper;
import jakarta.annotation.Resource;
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

    /**
     * 校验模块下重名
     *
     * @param testPlan
     */
    public void validateTestPlan(TestPlan testPlan) {
        if (StringUtils.equals(testPlan.getType(), TestPlanConstants.TEST_PLAN_TYPE_PLAN) && !StringUtils.equals(testPlan.getGroupId(), TestPlanConstants.TEST_PLAN_DEFAULT_GROUP_ID)) {
            TestPlan group = testPlanMapper.selectByPrimaryKey(testPlan.getGroupId());
            testPlan.setModuleId(group.getModuleId());
        }
        TestPlanExample example = new TestPlanExample();
        if (StringUtils.isBlank(testPlan.getId())) {
            //新建 校验
            example.createCriteria().andNameEqualTo(testPlan.getName()).andProjectIdEqualTo(testPlan.getProjectId()).andModuleIdEqualTo(testPlan.getModuleId());
            if (testPlanMapper.countByExample(example) > 0) {
                throw new MSException(Translator.get("test_plan.name.exist") + ":" + testPlan.getName());
            }
        } else {
            //更新 校验
            example.createCriteria().andNameEqualTo(testPlan.getName()).andProjectIdEqualTo(testPlan.getProjectId()).andIdNotEqualTo(testPlan.getId()).andModuleIdEqualTo(testPlan.getModuleId());
            if (testPlanMapper.countByExample(example) > 0) {
                throw new MSException(Translator.get("test_plan.name.exist") + ":" + testPlan.getName());
            }
        }
    }


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


}
