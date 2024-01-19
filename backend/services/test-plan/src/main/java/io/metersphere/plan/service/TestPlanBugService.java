package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanBugExample;
import io.metersphere.plan.mapper.TestPlanBugMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanBugService extends TestPlanResourceService {
    @Resource
    private TestPlanBugMapper testPlanBugMapper;

    @Override
    public int deleteBatchByTestPlanId(List<String> testPlanIdList) {
        if (CollectionUtils.isEmpty(testPlanIdList)) {
            return 0;
        }
        TestPlanBugExample example = new TestPlanBugExample();
        example.createCriteria().andTestPlanIdIn(testPlanIdList);
        return testPlanBugMapper.deleteByExample(example);
    }

    @Override
    public long getNextOrder(String testPlanId) {
        return 0;
    }

    @Override
    public void updatePos(String id, long pos) {

    }

    @Override
    public void refreshPos(String testPlanId) {

    }

}
