package io.metersphere.plan.service;

import io.metersphere.plan.dto.TestPlanResourceExecResultDTO;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.constants.ResultStatus;
import io.metersphere.sdk.constants.TestPlanConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.TestPlanModuleExample;
import io.metersphere.system.mapper.TestPlanModuleMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, Map<String, List<String>>> parseExecResult(List<TestPlanResourceExecResultDTO> execResults) {
        Map<String, Map<String, List<String>>> returnMap = new HashMap<>();
        for (TestPlanResourceExecResultDTO execResult : execResults) {
            String groupId = execResult.getTestPlanGroupId();
            String planId = execResult.getTestPlanId();

            Map<String, List<String>> planMap;
            if (returnMap.containsKey(groupId)) {
                planMap = returnMap.get(groupId);
                List<String> resultList;
                if (planMap.containsKey(planId)) {
                    resultList = planMap.get(planId);
                } else {
                    resultList = new ArrayList<>();
                }
                resultList.add(execResult.getExecResult());
                planMap.put(planId, resultList);
            } else {
                planMap = new HashMap<>();
                List<String> resultList = new ArrayList<>();
                resultList.add(execResult.getExecResult());
                planMap.put(planId, resultList);
            }
            returnMap.put(groupId, planMap);
        }
        return returnMap;
    }

    public String calculateTestPlanStatus(List<String> resultList) {
        //目前只有三个状态。如果同时包含多种状态(进行中/未开始、进行中/已完成、已完成/未开始、进行中/未开始/已完成),根据算法可得测试计划都会是进行中
        List<String> allStatus = new ArrayList<>();
        resultList.stream().distinct().forEach(item -> {
            if (StringUtils.equalsAnyIgnoreCase(item, ResultStatus.BLOCKED.name(), ResultStatus.FAKE_ERROR.name(), ResultStatus.ERROR.name(), ResultStatus.SUCCESS.name(), TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
                allStatus.add(TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED);
            } else {
                allStatus.add(TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED);
            }
        });
        List<String> calculateList = allStatus.stream().distinct().toList();
        if (calculateList.size() == 1) {
            if (calculateList.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
                return TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED;
            } else
                return TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED;
        } else if (CollectionUtils.isEmpty(calculateList)) {
            return TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED;
        } else {
            return TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY;
        }
    }

    public String calculateStatusByChildren(List<String> childStatus) {
        // 首先去重
        childStatus = childStatus.stream().distinct().toList();

		/*
		1:全部都未开始 则为未开始
		2:全部都已完成 则为已完成
		3:包含进行中 则为进行中
		4:未开始+已完成：进行中
		 */
        if (childStatus.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY)) {
            return TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY;
        } else if (childStatus.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED) && childStatus.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED)) {
            return TestPlanConstants.TEST_PLAN_SHOW_STATUS_UNDERWAY;
        } else if (childStatus.contains(TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED)) {
            return TestPlanConstants.TEST_PLAN_SHOW_STATUS_COMPLETED;
        } else {
            return TestPlanConstants.TEST_PLAN_SHOW_STATUS_PREPARED;
        }
    }
}
