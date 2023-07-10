package io.metersphere.sdk.log.service;

import io.metersphere.sdk.domain.OperationLogBlob;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.mapper.OperationLogBlobMapper;
import io.metersphere.sdk.mapper.OperationLogMapper;
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
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class OperationLogService {
    @Resource
    private OperationLogMapper operationLogMapper;
    @Resource
    private OperationLogBlobMapper operationLogBlobMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public void add(LogDTO log) {
        if (StringUtils.isBlank(log.getProjectId())) {
            log.setProjectId("none");
        }
        if (StringUtils.isBlank(log.getCreateUser())) {
            log.setCreateUser("admin");
        }
        // 限制长度
        saveBlob(operationLogMapper, operationLogBlobMapper, log);
    }

    public void batchAdd(List<LogDTO> logs) {
        if (CollectionUtils.isEmpty(logs)) {
            return;
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        OperationLogMapper logMapper = sqlSession.getMapper(OperationLogMapper.class);
        OperationLogBlobMapper logBlobMapper = sqlSession.getMapper(OperationLogBlobMapper.class);

        if (CollectionUtils.isNotEmpty(logs)) {
            logs.forEach(item -> {
                if (StringUtils.isBlank(item.getId())) {
                    item.setId(UUID.randomUUID().toString());
                }
                // 限制长度
                saveBlob(logMapper, logBlobMapper, item);
            });
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    private void saveBlob(OperationLogMapper logMapper, OperationLogBlobMapper logBlobMapper, LogDTO item) {
        if (StringUtils.isNotBlank(item.getContent()) && item.getContent().length() > 500) {
            item.setContent(item.getContent().substring(0, 499));
        }
        logMapper.insert(item);
        OperationLogBlob blob = new OperationLogBlob();
        blob.setId(item.getId());
        blob.setOriginalValue(item.getOriginalValue());
        blob.setModifiedValue(item.getModifiedValue());
        logBlobMapper.insert(blob);
    }
}
