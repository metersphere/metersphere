package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlan;
import io.metersphere.plan.domain.TestPlanFunctionalCase;
import io.metersphere.plan.domain.TestPlanFunctionalCaseExample;
import io.metersphere.plan.dto.TestPlanResourceAssociationParam;
import io.metersphere.plan.mapper.ExtTestPlanFunctionalCaseMapper;
import io.metersphere.plan.mapper.TestPlanFunctionalCaseMapper;
import io.metersphere.sdk.constants.ExecStatus;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.utils.ServiceUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanCaseService {

    @Resource
    private TestPlanFunctionalCaseMapper testPlanFunctionalCaseMapper;
    @Resource
    private ExtTestPlanFunctionalCaseMapper extTestPlanFunctionalCaseMapper;


    public void saveTestPlanResource(@Validated TestPlanResourceAssociationParam associationParam) {
        long pox = this.getNextOrder(associationParam.getTestPlanId());
        long now = System.currentTimeMillis();
        List<TestPlanFunctionalCase> testPlanFunctionalCaseList = new ArrayList<>();
        List<String> associationIdList = associationParam.getResourceIdList();
        // 批量添加时，按照列表顺序进行展示。所以这里将集合倒叙排列
        Collections.reverse(associationIdList);
        for (int i = 0; i < associationIdList.size(); i++) {
            TestPlanFunctionalCase testPlanFunctionalCase = new TestPlanFunctionalCase();
            testPlanFunctionalCase.setId(IDGenerator.nextStr());
            testPlanFunctionalCase.setTestPlanId(associationParam.getTestPlanId());
            testPlanFunctionalCase.setFunctionalCaseId(associationIdList.get(i));
            testPlanFunctionalCase.setPos(pox);
            testPlanFunctionalCase.setCreateTime(now);
            testPlanFunctionalCase.setCreateUser(associationParam.getOperator());
            testPlanFunctionalCase.setLastExecResult(ExecStatus.PENDING.name());
            testPlanFunctionalCase.setExecuteUser(associationParam.getOperator());
            testPlanFunctionalCaseList.add(testPlanFunctionalCase);
            pox += ServiceUtils.POS_STEP;
        }
        testPlanFunctionalCaseMapper.batchInsert(testPlanFunctionalCaseList);
    }


    public long getNextOrder(String testPlanId) {
        Long maxPos = extTestPlanFunctionalCaseMapper.getMaxPosByTestPlanId(testPlanId);
        if (maxPos == null) {
            //默认返回POS_STEP，不能直接返回0， 否则无法进行“前置”排序
            return ServiceUtils.POS_STEP;
        } else {
            return maxPos + ServiceUtils.POS_STEP;
        }
    }


    /**
     * 复制计划时，复制功能用例
     *
     * @param ids
     * @param testPlan
     */
    public void saveTestPlanByPlanId(List<String> ids, TestPlan testPlan) {
        TestPlanFunctionalCaseExample example = new TestPlanFunctionalCaseExample();
        example.createCriteria().andTestPlanIdIn(ids);
        List<TestPlanFunctionalCase> testPlanFunctionalCases = testPlanFunctionalCaseMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(testPlanFunctionalCases)) {
            Map<String, List<TestPlanFunctionalCase>> collect = testPlanFunctionalCases.stream().collect(Collectors.groupingBy(TestPlanFunctionalCase::getTestPlanId));
            List<TestPlanFunctionalCase> associateList = new ArrayList<>();
            ids.forEach(id -> {
                if (collect.containsKey(id)) {
                    saveCase(collect.get(id), associateList, testPlan, id);
                }
            });
            testPlanFunctionalCaseMapper.batchInsert(associateList);
        }
    }

    private void saveCase(List<TestPlanFunctionalCase> testPlanFunctionalCases, List<TestPlanFunctionalCase> associateList, TestPlan testPlan, String id) {
        AtomicLong pos = new AtomicLong(this.getNextOrder(id));
        testPlanFunctionalCases.forEach(item -> {
            TestPlanFunctionalCase functionalCase = new TestPlanFunctionalCase();
            functionalCase.setTestPlanId(testPlan.getId());
            functionalCase.setId(IDGenerator.nextStr());
            functionalCase.setCreateTime(System.currentTimeMillis());
            functionalCase.setCreateUser(testPlan.getCreateUser());
            functionalCase.setFunctionalCaseId(item.getFunctionalCaseId());
            functionalCase.setPos(pos.get());
            associateList.add(functionalCase);
            pos.updateAndGet(v -> v + ServiceUtils.POS_STEP);
        });
    }
}


