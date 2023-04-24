package io.metersphere.log.service;

import io.metersphere.base.domain.OperatingLogResource;
import io.metersphere.base.domain.OperatingLogWithBLOBs;
import io.metersphere.base.mapper.OperatingLogMapper;
import io.metersphere.base.mapper.OperatingLogResourceMapper;
import io.metersphere.base.mapper.ext.BaseOperatingLogMapper;
import io.metersphere.commons.constants.OperLogModule;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.i18n.Translator;
import io.metersphere.log.constants.OperatorLevel;
import io.metersphere.log.vo.OperatingLogDTO;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.OperatingLogRequest;
import io.metersphere.service.ServiceUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class OperatingLogService {
    @Resource
    private OperatingLogMapper operatingLogMapper;
    @Resource
    private BaseOperatingLogMapper baseOperatingLogMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public void create(OperatingLogWithBLOBs log, String sourceIds) {
        log.setSourceId(StringUtils.EMPTY);
        operatingLogMapper.insert(log);
        if (StringUtils.isNotEmpty(sourceIds)) {
            List<String> ids = new ArrayList<>();
            if (sourceIds.startsWith("[")) {
                ids = JSON.parseArray(sourceIds, String.class);
            } else {
                ids.add(sourceIds.replace("\"", StringUtils.EMPTY));
            }
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            OperatingLogResourceMapper batchMapper = sqlSession.getMapper(OperatingLogResourceMapper.class);
            if (CollectionUtils.isNotEmpty(ids)) {
                ids.forEach(item -> {
                    OperatingLogResource resource = new OperatingLogResource();
                    resource.setId(UUID.randomUUID().toString());
                    resource.setOperatingLogId(log.getId());
                    resource.setSourceId(item);
                    batchMapper.insert(resource);
                });
                sqlSession.flushStatements();
                if (sqlSession != null && sqlSessionFactory != null) {
                    SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
                }
            }
        }
    }

    public List<OperatingLogDTO> list(OperatingLogRequest request) {
        if (CollectionUtils.isNotEmpty(request.getTimes())) {
            request.setStartTime(request.getTimes().get(0));
            request.setEndTime(request.getTimes().get(1));
        }
        String level = request.getLevel();
        List<String> modules = new ArrayList<>(OperLogModule.PROJECT_MODULES);
        if (StringUtils.equals(level, OperatorLevel.WORKSPACE)) {
            modules.addAll(OperLogModule.WORKSPACE_MODULES);
        } else if (StringUtils.equals(level, OperatorLevel.SYSTEM)) {
            modules.addAll(OperLogModule.SYSTEM_MODULES);
            modules.addAll(OperLogModule.WORKSPACE_MODULES);
        }
        request.setLevelModules(modules);

        List<OperatingLogDTO> list = baseOperatingLogMapper.list(request);
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> userIds = list.stream().map(OperatingLogDTO::getOperUser).collect(Collectors.toList());
            List<String> projectIds = list.stream().map(OperatingLogDTO::getProjectId).collect(Collectors.toList());
            List<String> logIds = list.stream().map(OperatingLogDTO::getId).collect(Collectors.toList());
            Map<String, String> sourceMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(logIds)) {
                List<OperatingLogDTO> logDtoArr = baseOperatingLogMapper.findSourceIdByLogIds(logIds);
                // 如果重复是批量操作，置空sourceID
                sourceMap = logDtoArr.stream()
                        .collect(Collectors.toMap(OperatingLogDTO::getOperatingLogId, OperatingLogDTO::getSourceId, (val1, val2) -> StringUtils.EMPTY));
            }
            Map<String, String> userNameMap = ServiceUtils.getUserNameMap(userIds);
            Map<String, String> workspaceNameMap = ServiceUtils.getWorkspaceNameByProjectIds(projectIds);
            for (OperatingLogDTO dto : list) {
                dto.setUserName(userNameMap.getOrDefault(dto.getOperUser(), StringUtils.EMPTY));
                dto.setWorkspaceName(workspaceNameMap.getOrDefault(dto.getProjectId(), StringUtils.EMPTY));
                dto.setSourceId(sourceMap.getOrDefault(dto.getId(), StringUtils.EMPTY));
            }
        }
        return list;
    }

    public OperatingLogDTO get(String id) {
        OperatingLogWithBLOBs log = operatingLogMapper.selectByPrimaryKey(id);
        OperatingLogDTO dto = new OperatingLogDTO();
        BeanUtils.copyBean(dto, log);
        if (StringUtils.isNotEmpty(log.getOperContent())) {
            dto.setDetails(JSON.parseObject(log.getOperContent(), OperatingLogDetails.class));
        }
        return dto;
    }

    public List<OperatingLogDTO> findBySourceId(OperatingLogRequest request) {
        List<OperatingLogDTO> logWithBLOBs = new ArrayList<>();
        if (request.getModules().toString().contains(Translator.get("project_environment_setting"))) {
            logWithBLOBs = baseOperatingLogMapper.findBySourceIdEnv(request);
        } else {
            logWithBLOBs = baseOperatingLogMapper.findBySourceId(request);
        }
        List<OperatingLogDTO> dtos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(logWithBLOBs)) {
            for (OperatingLogDTO logWithBLOB : logWithBLOBs) {
                if (StringUtils.isNotEmpty(logWithBLOB.getOperContent())) {
                    logWithBLOB.setDetails(JSON.parseObject(logWithBLOB.getOperContent(), OperatingLogDetails.class));
                }
                if (CollectionUtils.isEmpty(logWithBLOB.getDetails().getColumns())) {
                    dtos.add(logWithBLOB);
                }
                if (StringUtils.isBlank(logWithBLOB.getUserName()) && StringUtils.isNotBlank(logWithBLOB.getOperUser())) {
                    logWithBLOB.setUserName(logWithBLOB.getOperUser());
                }
            }
        }
        if (CollectionUtils.isNotEmpty(dtos)) {
            logWithBLOBs.removeAll(dtos);
        }
        return logWithBLOBs;
    }
}
