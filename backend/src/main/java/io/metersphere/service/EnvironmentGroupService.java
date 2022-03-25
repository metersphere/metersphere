package io.metersphere.service;

import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.base.mapper.EnvironmentGroupMapper;
import io.metersphere.base.mapper.EnvironmentGroupProjectMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.ExtEnvironmentGroupMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.EnvironmentGroupRequest;
import io.metersphere.dto.EnvironmentGroupDTO;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author lyh
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class EnvironmentGroupService {

    @Resource
    private EnvironmentGroupMapper environmentGroupMapper;
    @Resource
    private ExtEnvironmentGroupMapper extEnvironmentGroupMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private EnvironmentGroupProjectMapper environmentGroupProjectMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ApiTestEnvironmentMapper apiTestEnvironmentMapper;
    @Resource
    private ApiAutomationService apiAutomationService;
    @Resource
    private EnvironmentGroupProjectService environmentGroupProjectService;

    public EnvironmentGroup add(EnvironmentGroupRequest request) {
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        this.checkEnvironmentGroup(request);

        request.setId(UUID.randomUUID().toString());
        request.setCreateTime(System.currentTimeMillis());
        request.setUpdateTime(System.currentTimeMillis());
        environmentGroupMapper.insertSelective(request);
        this.insertGroupProject(request);

        return request;
    }

    private void insertGroupProject(EnvironmentGroupRequest request) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        EnvironmentGroupProjectMapper mapper = sqlSession.getMapper(EnvironmentGroupProjectMapper.class);

        List<EnvironmentGroupProject> envGroupProject = request.getEnvGroupProject();
        if (CollectionUtils.isEmpty(envGroupProject)) {
            return;
        }
        List<String> distinctProjects = envGroupProject.stream().map(EnvironmentGroupProject::getProjectId).distinct().collect(Collectors.toList());
        if (envGroupProject.size() != distinctProjects.size()) {
            MSException.throwException(Translator.get("environment_group_has_duplicate_project"));
        }
        List<Project> projects = projectMapper.selectByExample(new ProjectExample());
        List<String> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());
        List<ApiTestEnvironment> environments = apiTestEnvironmentMapper.selectByExample(new ApiTestEnvironmentExample());
        List<String> environmentIds = environments.stream().map(ApiTestEnvironment::getId).collect(Collectors.toList());

        for (EnvironmentGroupProject egp : envGroupProject) {
            String projectId = egp.getProjectId();
            String environmentId = egp.getEnvironmentId();
            if (StringUtils.isBlank(projectId) || StringUtils.isBlank(environmentId)) {
                continue;
            }

            if (!projectIds.contains(projectId) || !environmentIds.contains(environmentId)) {
                continue;
            }

            EnvironmentGroupProjectExample example = new EnvironmentGroupProjectExample();
            example.createCriteria()
                    .andEnvironmentGroupIdEqualTo(request.getId())
                    .andProjectIdEqualTo(projectId);
            List<EnvironmentGroupProject> environmentGroupProjects = environmentGroupProjectMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(environmentGroupProjects)) {
                continue;
            }

            EnvironmentGroupProject e = new EnvironmentGroupProject();
            e.setId(UUID.randomUUID().toString());
            e.setEnvironmentGroupId(request.getId());
            e.setProjectId(projectId);
            e.setEnvironmentId(environmentId);
            mapper.insert(e);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public List<EnvironmentGroup> getList(EnvironmentGroupRequest request) {
        return extEnvironmentGroupMapper.getList(request);
    }

    public void delete(String id) {
        if (StringUtils.isBlank(id)) {
            return;
        }
        environmentGroupMapper.deleteByPrimaryKey(id);
        apiAutomationService.setScenarioEnvGroupIdNull(id);
        EnvironmentGroupProjectExample example = new EnvironmentGroupProjectExample();
        example.createCriteria().andEnvironmentGroupIdEqualTo(id);
        environmentGroupProjectMapper.deleteByExample(example);
    }

    public void deleteByWorkspaceId(String workspaceId) {
        if (StringUtils.isBlank(workspaceId)) {
            return;
        }
        EnvironmentGroupExample example = new EnvironmentGroupExample();
        example.createCriteria().andWorkspaceIdEqualTo(workspaceId);
        List<EnvironmentGroup> environmentGroups = environmentGroupMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(environmentGroups)) {
            environmentGroups.forEach(environmentGroup -> delete(environmentGroup.getId()));
        }
    }

    public EnvironmentGroup update(EnvironmentGroupRequest request) {
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        if (StringUtils.isNotBlank(request.getName())) {
            this.checkEnvironmentGroup(request);
        }
        request.setUpdateTime(System.currentTimeMillis());
        environmentGroupMapper.updateByPrimaryKeySelective(request);

        String envGroupId = request.getId();
        EnvironmentGroupProjectExample example = new EnvironmentGroupProjectExample();
        example.createCriteria().andEnvironmentGroupIdEqualTo(envGroupId);
        environmentGroupProjectMapper.deleteByExample(example);

        this.insertGroupProject(request);

        return request;
    }

    private void checkEnvironmentGroup(EnvironmentGroupRequest request) {
        String name = request.getName();
        if (StringUtils.isBlank(name)) {
            MSException.throwException(Translator.get("null_environment_group_name"));
        }

        EnvironmentGroupExample environmentGroupExample = new EnvironmentGroupExample();
        EnvironmentGroupExample.Criteria criteria = environmentGroupExample.createCriteria();
        criteria.andNameEqualTo(name)
                .andWorkspaceIdEqualTo(request.getWorkspaceId());
        if (StringUtils.isNotBlank(request.getId())) {
            criteria.andIdNotEqualTo(request.getId());
        }

        if (environmentGroupMapper.countByExample(environmentGroupExample) > 0) {
            MSException.throwException(Translator.get("environment_group_name")+request.getName()+Translator.get("environment_group_exist"));
        }
    }

    public List<EnvironmentGroup> getRelateProjectGroup(String projectId) {
        return extEnvironmentGroupMapper.getRelateProject(SessionUtils.getCurrentWorkspaceId(), projectId);
    }

    public void copy(String id) {
        if (StringUtils.isBlank(id)) {
            return;
        }
        EnvironmentGroup environmentGroup = environmentGroupMapper.selectByPrimaryKey(id);
        EnvironmentGroupRequest copy = new EnvironmentGroupRequest();
        String copyId = UUID.randomUUID().toString();
        copy.setId(copyId);

        String copyNameId = copyId.substring(0, 3);
        String copyName = environmentGroup.getName() + "_" + copyNameId + "_COPY";
        copy.setName(copyName);
        copy.setWorkspaceId(environmentGroup.getWorkspaceId());
        this.checkEnvironmentGroup(copy);

        copy.setCreateTime(System.currentTimeMillis());
        copy.setUpdateTime(System.currentTimeMillis());
        copy.setCreateUser(SessionUtils.getUserId());
        copy.setDescription(environmentGroup.getDescription());
        environmentGroupMapper.insertSelective(copy);

        EnvironmentGroupProjectExample example = new EnvironmentGroupProjectExample();
        example.createCriteria().andEnvironmentGroupIdEqualTo(id);
        List<EnvironmentGroupProject> environmentGroupProjects = environmentGroupProjectMapper.selectByExample(example);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        EnvironmentGroupProjectMapper mapper = sqlSession.getMapper(EnvironmentGroupProjectMapper.class);

        for (EnvironmentGroupProject environmentGroupProject : environmentGroupProjects) {
            environmentGroupProject.setId(UUID.randomUUID().toString());
            environmentGroupProject.setEnvironmentGroupId(copyId);
            mapper.insertSelective(environmentGroupProject);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public void modify(EnvironmentGroupRequest request) {
        if (StringUtils.isBlank(request.getId()) || StringUtils.isBlank(request.getName())) {
            return;
        }
        checkEnvironmentGroup(request);
        environmentGroupMapper.updateByPrimaryKeySelective(request);
    }

    public void batchAdd(EnvironmentGroupRequest request) {
        Map<String, String> map = request.getMap();
        if (map == null || map.isEmpty()) {
            return;
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        EnvironmentGroupProjectMapper mapper = sqlSession.getMapper(EnvironmentGroupProjectMapper.class);

        List<String> groupIds = request.getGroupIds();
        List<Project> projects = projectMapper.selectByExample(new ProjectExample());
        List<String> projectIds = projects.stream().map(Project::getId).collect(Collectors.toList());
        List<ApiTestEnvironment> apiTestEnvironments = apiTestEnvironmentMapper.selectByExample(new ApiTestEnvironmentExample());
        List<String> envIds = apiTestEnvironments.stream().map(ApiTestEnvironment::getId).collect(Collectors.toList());
        for (String groupId : groupIds) {
            EnvironmentGroup group = environmentGroupMapper.selectByPrimaryKey(groupId);
            if (group == null) {
                continue;
            }

            for (Map.Entry<String, String> entry : map.entrySet()) {
                String projectId = entry.getKey();
                String envId = entry.getValue();
                if (!projectIds.contains(projectId) || !envIds.contains(envId)) {
                    continue;
                }

                EnvironmentGroupProjectExample example = new EnvironmentGroupProjectExample();
                example.createCriteria().andEnvironmentGroupIdEqualTo(groupId)
                        .andProjectIdEqualTo(projectId);
                List<EnvironmentGroupProject> egpList = environmentGroupProjectMapper.selectByExample(example);
                if (CollectionUtils.isNotEmpty(egpList)) {
                    EnvironmentGroupProject environmentGroupProject = egpList.get(0);
                    environmentGroupProject.setEnvironmentId(envId);
                    mapper.updateByPrimaryKeySelective(environmentGroupProject);
                } else {
                    EnvironmentGroupProject egp = new EnvironmentGroupProject();
                    egp.setId(UUID.randomUUID().toString());
                    egp.setEnvironmentGroupId(groupId);
                    egp.setProjectId(projectId);
                    egp.setEnvironmentId(envId);
                    mapper.insertSelective(egp);
                }
            }
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public List<EnvironmentGroupDTO> getEnvOptionGroup(List<String> projectIds) {
        EnvironmentGroupExample example = new EnvironmentGroupExample();
        example.createCriteria().andWorkspaceIdEqualTo(SessionUtils.getCurrentWorkspaceId());
        List<EnvironmentGroupDTO> result = new ArrayList<>();
        List<EnvironmentGroup> groups = environmentGroupMapper.selectByExample(example);
        List<String> ids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(projectIds)) {
            List<Project> projects = projectMapper.selectByExample(new ProjectExample());
            List<String> allProjectIds = projects.stream().map(Project::getId).collect(Collectors.toList());
            ids = projectIds.stream().filter(allProjectIds::contains).collect(Collectors.toList());
        }
        for (EnvironmentGroup group : groups) {
            Map<String, String> envMap = environmentGroupProjectService.getEnvMap(group.getId());
            EnvironmentGroupDTO dto = new EnvironmentGroupDTO();
            BeanUtils.copyProperties(group, dto);
            if (CollectionUtils.isNotEmpty(ids)) {
                boolean b = envMap.keySet().containsAll(ids);
                if (BooleanUtils.isFalse(b)) {
                    dto.setDisabled(true);
                }
            }
            result.add(dto);
        }
        return result;
    }
}
