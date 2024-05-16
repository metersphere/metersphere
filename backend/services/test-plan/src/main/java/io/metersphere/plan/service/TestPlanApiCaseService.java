package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanApiCaseExample;
import io.metersphere.plan.mapper.ExtTestPlanApiCaseMapper;
import io.metersphere.plan.mapper.TestPlanApiCaseMapper;
import jakarta.annotation.Resource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanApiCaseService extends TestPlanResourceService {
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private TestPlanApiCaseMapper testPlanApiCaseMapper;
    @Resource
    private ExtTestPlanApiCaseMapper extTestPlanApiCaseMapper;

    @Override
    public int deleteBatchByTestPlanId(List<String> testPlanIdList) {
        TestPlanApiCaseExample example = new TestPlanApiCaseExample();
        example.createCriteria().andTestPlanIdIn(testPlanIdList);
        return testPlanApiCaseMapper.deleteByExample(example);
    }

    @Override
    public long getNextOrder(String projectId) {
        return 0;
    }

    @Override
    public void updatePos(String id, long pos) {
        // TODO
        //extTestPlanApiCaseMapper.updatePos(id, pos);
    }

    @Override
    public void refreshPos(String testPlanId) {
        // TODO
        /*List<String> caseIdList = extTestPlanApiCaseMapper.selectIdByTestPlanIdOrderByPos(testPlanId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtTestPlanApiCaseMapper batchUpdateMapper = sqlSession.getMapper(ExtTestPlanApiCaseMapper.class);
        for (int i = 0; i < caseIdList.size(); i++) {
            batchUpdateMapper.updatePos(caseIdList.get(i), i * DEFAULT_NODE_INTERVAL_POS);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);*/
    }


}
