package io.metersphere.api.service;

import io.metersphere.base.domain.ApiDefinitionExecResult;
import io.metersphere.base.mapper.ApiDefinitionExecResultMapper;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class ApiCaseResultService {
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void batchSave(Map<String, ApiDefinitionExecResult> executeQueue) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDefinitionExecResultMapper batchMapper = sqlSession.getMapper(ApiDefinitionExecResultMapper.class);
        for (String testId : executeQueue.keySet()) {
            ApiDefinitionExecResult report = executeQueue.get(testId);
            batchMapper.insert(report);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }
}
