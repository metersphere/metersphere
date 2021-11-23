package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiTestEnvironmentMapper;
import io.metersphere.base.mapper.EnvironmentGroupMapper;
import io.metersphere.base.mapper.EnvironmentGroupProjectMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.ExtEnvironmentGroupMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.EnvironmentGroupRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
        // todo 项目重复校验
        for (EnvironmentGroupProject egp : envGroupProject) {
            String projectId = egp.getProjectId();
            String environmentId = egp.getEnvironmentId();
            if (StringUtils.isBlank(projectId) || StringUtils.isBlank(environmentId)) {
                continue;
            }
            EnvironmentGroupProject e = new EnvironmentGroupProject();
            // todo 检查 项目｜环境 是否存在
            e.setId(UUID.randomUUID().toString());
            e.setDescription(egp.getDescription());
            e.setEnvironmentGroupId(request.getId());
            e.setProjectId(projectId);
            e.setEnvironmentId(environmentId);
            mapper.insert(e);
        }
        sqlSession.flushStatements();
    }

    public List<EnvironmentGroup> getList(EnvironmentGroupRequest request) {
        return extEnvironmentGroupMapper.getList(request);
    }

    public void delete(String id) {
        if (StringUtils.isBlank(id)) {
            return;
        }
        environmentGroupMapper.deleteByPrimaryKey(id);
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
            environmentGroups.forEach(environmentGroup -> {
                delete(environmentGroup.getId());
            });
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
            MSException.throwException("environment group name is null.");
        }

        EnvironmentGroupExample environmentGroupExample = new EnvironmentGroupExample();
        EnvironmentGroupExample.Criteria criteria = environmentGroupExample.createCriteria();
        criteria.andNameEqualTo(name)
                .andWorkspaceIdEqualTo(request.getWorkspaceId());
        if (StringUtils.isNotBlank(request.getId())) {
            criteria.andIdNotEqualTo(request.getId());
        }

        if (environmentGroupMapper.countByExample(environmentGroupExample) > 0) {
            MSException.throwException("环境组名称 " + request.getName() + " 已存在！");
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
    }
}
