package io.metersphere.track.service;

import io.metersphere.base.domain.TestPlanExecutionQueue;
import io.metersphere.base.mapper.ext.ExtTestPlanExecutionQueueMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
public class TestPlanExecutionQueueService {

    @Resource
    private ExtTestPlanExecutionQueueMapper extTestPlanExecutionQueueMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void batchSave(List<TestPlanExecutionQueue> planExecutionQueues) {
        if (!planExecutionQueues.isEmpty()) {
            extTestPlanExecutionQueueMapper.sqlInsert(planExecutionQueues);
        }
    }

    public int getNextNum(String resourceId) {
        TestPlanExecutionQueue testPlanExecutionQueue = extTestPlanExecutionQueueMapper.getNextNum(resourceId);
        if (testPlanExecutionQueue == null || testPlanExecutionQueue.getNum() == null) {
            return 100001;
        } else {
            return Optional.of(testPlanExecutionQueue.getNum() + 1).orElse(100001);
        }
    }

}
