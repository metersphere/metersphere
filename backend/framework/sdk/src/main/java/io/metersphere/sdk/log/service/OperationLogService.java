package io.metersphere.sdk.log.service;

import io.metersphere.project.domain.Project;
import io.metersphere.sdk.domain.OperationLogBlob;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.log.vo.OperationLogRequest;
import io.metersphere.sdk.log.vo.OperationLogResponse;
import io.metersphere.sdk.mapper.*;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Organization;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class OperationLogService {
    @Resource
    private OperationLogMapper operationLogMapper;
    @Resource
    private OperationLogBlobMapper operationLogBlobMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Resource
    private BaseOperationLogMapper baseOperationLogMapper;

    @Resource
    private BaseUserMapper baseUserMapper;

    @Resource
    private BaseProjectMapper baseProjectMapper;

    @Resource
    private BaseOrganizationMapper baseOrganizationMapper;

    public void add(LogDTO log) {
        if (StringUtils.isBlank(log.getProjectId())) {
            log.setProjectId("none");
        }
        if (StringUtils.isBlank(log.getCreateUser())) {
            log.setCreateUser("admin");
        }
        // 限制长度
        log.setBatchId(UUID.randomUUID().toString());
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
            String batchId = UUID.randomUUID().toString();
            logs.forEach(item -> {
                item.setBatchId(batchId);
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


    public List<OperationLogResponse> list(OperationLogRequest request) {
        int compare = Long.compare(request.getStartTime(), request.getEndTime());
        if (compare > 0) {
            throw new MSException(Translator.get("startTime_must_be_less_than_endTime"));
        }
        List<OperationLogResponse> list = baseOperationLogMapper.list(request);

        if (CollectionUtils.isNotEmpty(list)) {
            List<String> userIds = list.stream().map(OperationLogResponse::getCreateUser).collect(Collectors.toList());
            List<String> projectIds = list.stream().map(OperationLogResponse::getProjectId).collect(Collectors.toList());
            List<String> organizationIds = list.stream().map(OperationLogResponse::getOrganizationId).collect(Collectors.toList());
            List<OptionDTO> userList = baseUserMapper.selectUserOptionByIds(userIds);
            Map<String, String> userMap = userList.stream().collect(Collectors.toMap(OptionDTO::getId, OptionDTO::getName));
            List<Project> projects = baseProjectMapper.selectProjectByIdList(projectIds);
            Map<String, String> projectMap = projects.stream().collect(Collectors.toMap(Project::getId, Project::getName));
            List<Organization> organizations = baseOrganizationMapper.selectOrganizationByIdList(organizationIds);
            Map<String, String> organizationMap = organizations.stream().collect(Collectors.toMap(Organization::getId, Organization::getName));
            list.forEach(item -> {
                item.setUserName(userMap.getOrDefault(item.getCreateUser(), StringUtils.EMPTY));
                item.setProjectName(projectMap.getOrDefault(item.getProjectId(), StringUtils.EMPTY));
                item.setOrganizationName(organizationMap.getOrDefault(item.getOrganizationId(), StringUtils.EMPTY));
            });


        }

        return list;
    }
}
