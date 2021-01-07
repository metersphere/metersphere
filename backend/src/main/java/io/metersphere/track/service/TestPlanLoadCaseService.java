package io.metersphere.track.service;

import io.metersphere.base.domain.LoadTest;
import io.metersphere.base.domain.TestPlanLoadCase;
import io.metersphere.base.domain.TestPlanLoadCaseExample;
import io.metersphere.base.mapper.TestPlanLoadCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanLoadCaseMapper;
import io.metersphere.performance.service.PerformanceTestService;
import io.metersphere.track.dto.TestPlanLoadCaseDTO;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanLoadCaseService {

    @Resource
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;
    @Resource
    private ExtTestPlanLoadCaseMapper extTestPlanLoadCaseMapper;
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public List<LoadTest> relevanceList(LoadCaseRequest request) {
        List<String> ids = extTestPlanLoadCaseMapper.selectIdsNotInPlan(request.getProjectId(), request.getTestPlanId());
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return performanceTestService.getLoadTestListByIds(ids);
    }

    public List<TestPlanLoadCaseDTO> list(LoadCaseRequest request) {
        return extTestPlanLoadCaseMapper.selectTestPlanLoadCaseList(request.getTestPlanId());
    }

    public void relevanceCase(LoadCaseRequest request) {
        List<String> caseIds = request.getCaseIds();
        String planId = request.getTestPlanId();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);

        TestPlanLoadCaseMapper testPlanLoadCaseMapper = sqlSession.getMapper(TestPlanLoadCaseMapper.class);
        caseIds.forEach(id -> {
            TestPlanLoadCase t = new TestPlanLoadCase();
            t.setId(UUID.randomUUID().toString());
            t.setTestPlanId(planId);
            t.setLoadCaseId(id);
            t.setCreateTime(System.currentTimeMillis());
            t.setUpdateTime(System.currentTimeMillis());
            testPlanLoadCaseMapper.insert(t);
        });
        sqlSession.flushStatements();
    }

    public void delete(String id) {
        TestPlanLoadCaseExample testPlanLoadCaseExample = new TestPlanLoadCaseExample();
        testPlanLoadCaseExample.createCriteria().andIdEqualTo(id);
        testPlanLoadCaseMapper.deleteByExample(testPlanLoadCaseExample);
    }
}
