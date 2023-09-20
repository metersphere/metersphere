package io.metersphere.plan.service;

import io.metersphere.plan.domain.*;
import io.metersphere.plan.dto.TestPlanDTO;
import io.metersphere.plan.mapper.ExtTestPlanMapper;
import io.metersphere.plan.mapper.TestPlanConfigMapper;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.utils.SessionUtils;
import io.metersphere.system.domain.User;
import io.metersphere.system.uid.UUID;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanService {
    @Resource
    private TestPlanMapper testPlanMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private TestPlanConfigMapper testPlanConfigMapper;

    @Resource
    private TestPlanConfigService testPlanConfigService;
    @Resource
    private TestPlanPrincipalService testPlanPrincipalService;
    @Resource
    private TestPlanFollowerService testPlanFollowerService;
    @Resource
    private TestPlanApiCaseService testPlanApiCaseService;
    @Resource
    private TestPlanApiScenarioService testPlanApiScenarioService;
    @Resource
    private TestPlanLoadCaseService testPlanLoadCaseService;
    @Resource
    private TestPlanUiScenarioService testPlanUiScenarioService;
    @Resource
    private TestPlanFunctionCaseService testPlanFunctionCaseService;

    public TestPlanDTO add(@NotNull TestPlanDTO testPlanCreateRequest) {
        if (StringUtils.equals(testPlanCreateRequest.getParentId(), testPlanCreateRequest.getId())) {
            throw new MSException("The parent test plan cannot be the same as the current test plan!");
        }

        if (StringUtils.isBlank(testPlanCreateRequest.getId())) {
            testPlanCreateRequest.setId(UUID.randomUUID().toString());
        }
        testPlanCreateRequest.setCreateTime(System.currentTimeMillis());
        testPlanCreateRequest.setUpdateTime(System.currentTimeMillis());

        TestPlan testPlan = new TestPlan();
        BeanUtils.copyBean(testPlan, testPlanCreateRequest);
        testPlanMapper.insert(testPlan);

        TestPlanConfig testPlanConfig = new TestPlanConfig();
        testPlanConfig.setTestPlanId(testPlan.getId());
        testPlanConfig.setAutomaticStatusUpdate(testPlanCreateRequest.isAutomaticStatusUpdate());
        testPlanConfig.setRepeatCase(testPlanCreateRequest.isRepeatCase());
        testPlanConfig.setPassThreshold(testPlanCreateRequest.getPassThreshold());
        testPlanConfigMapper.insert(testPlanConfig);

        if (CollectionUtils.isNotEmpty(testPlanCreateRequest.getFollowers())) {
            List<TestPlanFollower> testPlanFollowerList = new ArrayList<>();
            for (String follower : testPlanCreateRequest.getFollowers()) {
                TestPlanFollower testPlanFollower = new TestPlanFollower();
                testPlanFollower.setTestPlanId(testPlan.getId());
                testPlanFollower.setUserId(follower);
                testPlanFollowerList.add(testPlanFollower);
            }
            testPlanFollowerService.batchSave(testPlanFollowerList);
        }

        if (CollectionUtils.isNotEmpty(testPlanCreateRequest.getPrincipals())) {
            List<TestPlanPrincipal> testPlanPrincipalList = new ArrayList<>();
            for (String principal : testPlanCreateRequest.getPrincipals()) {
                TestPlanPrincipal testPlanPrincipal = new TestPlanPrincipal();
                testPlanPrincipal.setTestPlanId(testPlan.getId());
                testPlanPrincipal.setUserId(principal);
                testPlanPrincipalList.add(testPlanPrincipal);
            }
            testPlanPrincipalService.batchSave(testPlanPrincipalList);
        }
        return testPlanCreateRequest;
    }

    public void batchDelete(List<String> idList) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andIdIn(idList);
        testPlanMapper.deleteByExample(example);

        this.cascadeDelete(idList);
    }

    public void delete(@NotBlank String id) {
        TestPlanExample example = new TestPlanExample();
        example.createCriteria().andIdEqualTo(id);
        testPlanMapper.deleteByExample(example);
        this.cascadeDelete(id);
    }

    public void deleteByParentId(String parentTestPlanId) {
        List<String> childrenTestPlanIdList = extTestPlanMapper.selectByParentId(parentTestPlanId);
        if (CollectionUtils.isNotEmpty(childrenTestPlanIdList)) {
            this.batchDelete(childrenTestPlanIdList);
        }
    }

    @Validated
    public void deleteBatchByParentId(List<String> parentTestPlanId) {
        List<String> childrenTestPlanIdList = extTestPlanMapper.selectByParentIdList(parentTestPlanId);
        if (CollectionUtils.isNotEmpty(childrenTestPlanIdList)) {
            this.batchDelete(childrenTestPlanIdList);
        }
    }

    private void cascadeDelete(String id) {
        //删除子计划
        this.deleteByParentId(id);
        //删除当前计划对应的资源
        this.testPlanConfigService.delete(id);
        this.testPlanFunctionCaseService.deleteByTestPlanId(id);
        this.testPlanApiCaseService.deleteByTestPlanId(id);
        this.testPlanApiScenarioService.deleteByTestPlanId(id);
        this.testPlanUiScenarioService.deleteByTestPlanId(id);
        this.testPlanLoadCaseService.deleteByTestPlanId(id);
    }

    private void cascadeDelete(List<String> idList) {
        //删除子计划
        this.deleteBatchByParentId(idList);
        //删除当前计划对应的资源
        this.testPlanConfigService.deleteBatch(idList);
        this.testPlanFunctionCaseService.deleteBatchByTestPlanId(idList);
        this.testPlanApiCaseService.deleteBatchByTestPlanId(idList);
        this.testPlanApiScenarioService.deleteBatchByTestPlanId(idList);
        this.testPlanUiScenarioService.deleteBatchByTestPlanId(idList);
        this.testPlanLoadCaseService.deleteBatchByTestPlanId(idList);
    }
}
