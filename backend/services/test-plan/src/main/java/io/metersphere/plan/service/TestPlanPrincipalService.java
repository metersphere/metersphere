package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanPrincipal;
import io.metersphere.plan.mapper.TestPlanPrincipalMapper;
import jakarta.annotation.Resource;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class TestPlanPrincipalService {

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public void batchSave(List<TestPlanPrincipal> testPlanPrincipalList) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        TestPlanPrincipalMapper testPlanPrincipalMapper = sqlSession.getMapper(TestPlanPrincipalMapper.class);
        try {
            int insertIndex = 0;
            for (TestPlanPrincipal testPlanPrincipal : testPlanPrincipalList) {
                testPlanPrincipalMapper.insert(testPlanPrincipal);
                insertIndex++;
                if (insertIndex % 50 == 0) {
                    sqlSession.flushStatements();
                }
            }
            sqlSession.flushStatements();
        } finally {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }
}
