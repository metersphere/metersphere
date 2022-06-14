package io.metersphere.log.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.OperatingLogResource;
import io.metersphere.base.domain.OperatingLogWithBLOBs;
import io.metersphere.base.mapper.OperatingLogMapper;
import io.metersphere.base.mapper.OperatingLogResourceMapper;
import io.metersphere.base.mapper.ext.ExtOperatingLogMapper;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.log.vo.OperatingLogDTO;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.OperatingLogRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class OperatingLogService {
    @Resource
    private OperatingLogMapper operatingLogMapper;
    @Resource
    private ExtOperatingLogMapper extOperatingLogMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;

    public void create(OperatingLogWithBLOBs log, String sourceIds) {
        log.setSourceId("");
        operatingLogMapper.insert(log);
        if (StringUtils.isNotEmpty(sourceIds)) {
            List<String> ids = new ArrayList<>();
            if (sourceIds.startsWith("[")) {
                ids = JSON.parseObject(sourceIds, List.class);
            } else {
                ids.add(sourceIds.replace("\"", ""));
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
        List<OperatingLogDTO> list = extOperatingLogMapper.list(request);
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> userIds = list.stream().map(OperatingLogDTO::getOperUser).collect(Collectors.toList());
            List<String> projectIds = list.stream().map(OperatingLogDTO::getProjectId).collect(Collectors.toList());
            List<String> logIds = list.stream().map(OperatingLogDTO::getId).collect(Collectors.toList());
            Map<String, String> sourceMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(logIds)) {
                List<OperatingLogDTO> logDtoArr = extOperatingLogMapper.findSourceIdByLogIds(logIds);
                // 如果重复是批量操作，置空sourceID
                sourceMap = logDtoArr.stream()
                        .collect(Collectors.toMap(OperatingLogDTO::getOperatingLogId, OperatingLogDTO::getSourceId, (val1, val2) -> ""));
            }
            Map<String, String> userNameMap = ServiceUtils.getUserNameMap(userIds);
            Map<String, String> workspaceNameMap = ServiceUtils.getWorkspaceNameByProjectIds(projectIds);
            for (OperatingLogDTO dto : list) {
                dto.setUserName(userNameMap.getOrDefault(dto.getOperUser(), ""));
                dto.setWorkspaceName(workspaceNameMap.getOrDefault(dto.getProjectId(), ""));
                dto.setSourceId(sourceMap.getOrDefault(dto.getId(), ""));
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
            logWithBLOBs = extOperatingLogMapper.findBySourceIdEnv(request);
        } else {
            logWithBLOBs = extOperatingLogMapper.findBySourceId(request);
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

            }
        }
        if (CollectionUtils.isNotEmpty(dtos)) {
            logWithBLOBs.removeAll(dtos);
        }
        return logWithBLOBs;
    }
}
