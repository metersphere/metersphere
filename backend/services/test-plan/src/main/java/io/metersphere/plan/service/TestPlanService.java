package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanFollower;
import io.metersphere.plan.domain.TestPlanPrincipal;
import io.metersphere.plan.dto.TestPlanDTO;
import io.metersphere.plan.mapper.TestPlanMapper;
import io.metersphere.sdk.util.BeanUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanService {
    @Resource
    private TestPlanMapper testPlanMapper;

    @Resource
    private TestPlanPrincipalService testPlanPrincipalService;
    @Resource
    private TestPlanFollowerService testPlanFollowerService;

    public TestPlanDTO add(@NotNull TestPlanDTO testPlanDTO) {
        TestPlan testPlan = new TestPlan();
        BeanUtils.copyBean(testPlan, testPlanDTO);
        testPlan.setId(UUID.randomUUID().toString());
        //todo SongTianyang:暂时没有SessionUtil，创建人先根据前台传值保存
        testPlan.setCreateTime(System.currentTimeMillis());
        testPlanMapper.insert(testPlan);

        if (CollectionUtils.isNotEmpty(testPlanDTO.getFollowers())) {
            List<TestPlanFollower> testPlanFollowerList = new ArrayList<>();
            for (String follower : testPlanDTO.getFollowers()) {
                TestPlanFollower testPlanFollower = new TestPlanFollower();
                testPlanFollower.setTestPlanId(testPlan.getId());
                testPlanFollower.setUserId(follower);
                testPlanFollowerList.add(testPlanFollower);
            }
            testPlanFollowerService.batchSave(testPlanFollowerList);
        }

        if (CollectionUtils.isNotEmpty(testPlanDTO.getPrincipals())) {
            List<TestPlanPrincipal> testPlanPrincipalList = new ArrayList<>();
            for (String principal : testPlanDTO.getPrincipals()) {
                TestPlanPrincipal testPlanPrincipal = new TestPlanPrincipal();
                testPlanPrincipal.setTestPlanId(testPlan.getId());
                testPlanPrincipal.setUserId(principal);
                testPlanPrincipalList.add(testPlanPrincipal);
            }
            testPlanPrincipalService.batchSave(testPlanPrincipalList);
        }
        return testPlanDTO;
    }
}
