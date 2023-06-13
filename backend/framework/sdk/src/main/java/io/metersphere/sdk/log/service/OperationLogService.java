package io.metersphere.sdk.log.service;

import io.metersphere.system.domain.OperationLog;
import io.metersphere.system.mapper.OperationLogMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class OperationLogService {
    @Resource
    private OperationLogMapper operationLogMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public void add(OperationLog log) {
        if (StringUtils.isBlank(log.getProjectId())) {
            log.setProjectId("none");
        }
        if (StringUtils.isBlank(log.getCreateUser())) {
            log.setCreateUser("admin");
        }
        // 限制长度
        if (StringUtils.isNotBlank(log.getDetails()) && log.getDetails().length() > 500) {
            log.setDetails(log.getDetails().substring(0, 499));
        }
        operationLogMapper.insert(log);
    }

    public void batchAdd(List<OperationLog> logs) {
        if (CollectionUtils.isEmpty(logs)) {
            return;
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        OperationLogMapper logMapper = sqlSession.getMapper(OperationLogMapper.class);
        if (CollectionUtils.isNotEmpty(logs)) {
            logs.forEach(item -> {
                // 限制长度
                if (StringUtils.isNotBlank(item.getDetails()) && item.getDetails().length() > 500) {
                    item.setDetails(item.getDetails().substring(0, 499));
                }
                logMapper.insert(item);
            });
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }
}
