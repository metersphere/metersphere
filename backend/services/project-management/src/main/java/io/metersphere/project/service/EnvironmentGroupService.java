package io.metersphere.project.service;


import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.dto.MoveNodeSortDTO;
import io.metersphere.project.dto.environment.*;
import io.metersphere.project.mapper.ExtEnvironmentMapper;
import io.metersphere.project.mapper.ExtProjectMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.domain.*;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.mapper.EnvironmentGroupMapper;
import io.metersphere.sdk.mapper.EnvironmentGroupRelationMapper;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.CommonBeanFactory;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.dto.sdk.BaseSystemConfigDTO;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.NodeMoveRequest;
import io.metersphere.system.dto.sdk.request.PosRequest;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.service.SystemParameterService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class EnvironmentGroupService extends MoveNodeService{

    @Resource
    private EnvironmentGroupMapper environmentGroupMapper;
    @Resource
    private ExtEnvironmentMapper extEnvironmentMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private EnvironmentGroupRelationMapper environmentGroupRelationMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private ExtProjectMapper extProjectMapper;
    @Resource
    private EnvironmentService environmentService;
    private static final String MOCK_EVN_SOCKET = "/mock-server/";

    public EnvironmentGroup add(EnvironmentGroupRequest request, String userId) {
        EnvironmentGroup environmentGroup = new EnvironmentGroup();
        BeanUtils.copyBean(environmentGroup, request);
        environmentGroup.setId(IDGenerator.nextStr());
        this.checkEnvironmentGroup(environmentGroup);

        environmentGroup.setCreateTime(System.currentTimeMillis());
        environmentGroup.setUpdateTime(System.currentTimeMillis());
        environmentGroup.setCreateUser(userId);
        environmentGroup.setUpdateUser(userId);
        environmentGroup.setPos(getNextOrder(request.getProjectId()));
        environmentGroupMapper.insertSelective(environmentGroup);
        request.setId(environmentGroup.getId());
        if (CollectionUtils.isNotEmpty(request.getEnvGroupProject())) {
            this.insertGroupProject(request);
        }

        return environmentGroup;
    }

    public long getNextOrder(String projectId) {
        Long pos = extEnvironmentMapper.getGroupPos(projectId);
        return (pos == null ? 0 : pos) + DEFAULT_NODE_INTERVAL_POS;
    }

    private void insertGroupProject(EnvironmentGroupRequest request) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        EnvironmentGroupRelationMapper mapper = sqlSession.getMapper(EnvironmentGroupRelationMapper.class);
        List<EnvironmentGroupProjectDTO> envGroupProject = request.getEnvGroupProject();
        List<String> distinctProjects = envGroupProject.stream().map(EnvironmentGroupProjectDTO::getProjectId).distinct().toList();
        if (envGroupProject.size() != distinctProjects.size()) {
            throw new MSException(Translator.get("environment_group_has_duplicate_project"));
        }
        // 校验项目是否存在  检查环境是否存在
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andIdIn(distinctProjects).andEnableEqualTo(true);
        if (projectMapper.countByExample(projectExample) != distinctProjects.size()) {
            throw new MSException(Translator.get("project_not_exist"));
        }
        List<String> envIds = envGroupProject.stream().map(EnvironmentGroupProjectDTO::getEnvironmentId).distinct().toList();
        EnvironmentExample environmentExample = new EnvironmentExample();
        environmentExample.createCriteria().andIdIn(envIds);
        if (environmentMapper.countByExample(environmentExample) != envIds.size()) {
            throw new MSException(Translator.get("api_test_environment_not_exist"));
        }
        EnvironmentGroupRelationExample example = new EnvironmentGroupRelationExample();
        example.createCriteria()
                .andEnvironmentGroupIdEqualTo(request.getId());
        if (environmentGroupRelationMapper.countByExample(example) > 0) {
            environmentGroupRelationMapper.deleteByExample(example);
        }
        for (EnvironmentGroupProjectDTO egp : envGroupProject) {
            EnvironmentGroupRelation e = new EnvironmentGroupRelation();
            e.setId(IDGenerator.nextStr());
            e.setEnvironmentGroupId(request.getId());
            e.setProjectId(egp.getProjectId());
            e.setEnvironmentId(egp.getEnvironmentId());
            mapper.insert(e);
        }
        sqlSession.flushStatements();
        if (sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public void delete(String id) {
        checkEnvironmentGroup(id);
        environmentGroupMapper.deleteByPrimaryKey(id);
        EnvironmentGroupRelationExample example = new EnvironmentGroupRelationExample();
        example.createCriteria().andEnvironmentGroupIdEqualTo(id);
        environmentGroupRelationMapper.deleteByExample(example);
    }

    private EnvironmentGroup checkEnvironmentGroup(String id) {
        EnvironmentGroup environmentGroup = environmentGroupMapper.selectByPrimaryKey(id);
        if (environmentGroup == null) {
            throw new MSException(Translator.get("null_environment_group_name"));
        }
        return environmentGroup;
    }

    public EnvironmentGroup update(EnvironmentGroupRequest request, String userId) {
        if (StringUtils.isBlank(request.getId())) {
            throw new MSException(Translator.get("project_version.id.not_blank"));
        }
        EnvironmentGroup environmentGroup = checkEnvironmentGroup(request.getId());
        environmentGroup.setName(request.getName());
        this.checkEnvironmentGroup(environmentGroup);
        environmentGroup.setUpdateTime(System.currentTimeMillis());
        environmentGroup.setUpdateUser(userId);
        environmentGroup.setDescription(request.getDescription());
        environmentGroupMapper.updateByPrimaryKeySelective(environmentGroup);
        if (CollectionUtils.isNotEmpty(request.getEnvGroupProject())) {
            this.insertGroupProject(request);
        } else {
            EnvironmentGroupRelationExample example = new EnvironmentGroupRelationExample();
            example.createCriteria().andEnvironmentGroupIdEqualTo(request.getId());
            environmentGroupRelationMapper.deleteByExample(example);
        }

        return environmentGroup;
    }

    private void checkEnvironmentGroup(EnvironmentGroup group) {
        EnvironmentGroupExample environmentGroupExample = new EnvironmentGroupExample();
        EnvironmentGroupExample.Criteria criteria = environmentGroupExample.createCriteria();
        criteria.andNameEqualTo(group.getName()).andProjectIdEqualTo(group.getProjectId()).andIdNotEqualTo(group.getId());
        if (environmentGroupMapper.countByExample(environmentGroupExample) > 0) {
            throw new MSException(Translator.get("environment_group_name") + group.getName() + Translator.get("environment_group_exist"));
        }
    }

    public List<EnvironmentGroup> list(EnvironmentFilterRequest request) {
        return extEnvironmentMapper.groupList(request.getKeyword(), request.getProjectId());
    }

    public EnvironmentGroupDTO get(String groupId) {
        EnvironmentGroupDTO environmentGroupDTO = new EnvironmentGroupDTO();
        EnvironmentGroup environmentGroup = checkEnvironmentGroup(groupId);
        EnvironmentGroupRelationExample example = new EnvironmentGroupRelationExample();
        example.createCriteria().andEnvironmentGroupIdEqualTo(groupId);
        List<EnvironmentGroupRelation> relations = environmentGroupRelationMapper.selectByExample(example);
        //提取环境id
        List<String> envIds = relations.stream().map(EnvironmentGroupRelation::getEnvironmentId).distinct().toList();
        List<Environment> environments = environmentService.getEnvironmentsByIds(envIds);
        Map<String, Environment> envMap = environments.stream().collect(Collectors.toMap(Environment::getId, e -> e));
        List<EnvironmentBlob> environmentBlobs = environmentService.getEnvironmentBlobsByIds(envIds);
        Map<String, EnvironmentBlob> envBlobMap = environmentBlobs.stream().collect(Collectors.toMap(EnvironmentBlob::getId, e -> e));
        List<EnvironmentGroupInfo> result = new ArrayList<>();
        relations.forEach(e -> {
            EnvironmentGroupInfo dto = new EnvironmentGroupInfo();
            dto.setProjectId(e.getProjectId());
            dto.setEnvironmentId(e.getEnvironmentId());
            Environment environment = envMap.get(e.getEnvironmentId());
            if (environment != null) {
                dto.setEnvName(environment.getName());
                dto.setDescription(environment.getDescription());
            }
            EnvironmentBlob environmentBlob = envBlobMap.get(e.getEnvironmentId());
            if (environmentBlob != null) {
                EnvironmentConfig environmentConfig = JSON.parseObject(new String(environmentBlob.getConfig()), EnvironmentConfig.class);
                dto.setDomain(ObjectUtils.isNotEmpty(environmentConfig) ? environmentConfig.getHttpConfig() : new ArrayList<>());
            }
            if (environment != null && BooleanUtils.isTrue(environment.getMock())) {
                SystemParameterService systemParameterService = CommonBeanFactory.getBean(SystemParameterService.class);
                if (systemParameterService != null) {
                    BaseSystemConfigDTO baseSystemConfigDTO = systemParameterService.getBaseInfo();
                    String baseUrl = baseSystemConfigDTO.getUrl();
                    if (StringUtils.isNotEmpty(baseUrl)) {
                        Project project = projectMapper.selectByPrimaryKey(environment.getProjectId());
                        dto.getDomain().getFirst().setHostname(StringUtils.join(baseUrl, MOCK_EVN_SOCKET, project.getNum()));
                    }
                }
            }
            result.add(dto);
        });
        BeanUtils.copyBean(environmentGroupDTO, environmentGroup);
        environmentGroupDTO.setEnvironmentGroupInfo(result);
        return environmentGroupDTO;
    }

    public List<OptionDTO> getProject(String userId, String organizationId) {
        //判断用户是否是系统管理员
        List<OptionDTO> result = new ArrayList<>();
        List<Project> projects = new ArrayList<>();
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdEqualTo(userId).andRoleIdEqualTo(InternalUserRole.ADMIN.name());
        if (userRoleRelationMapper.countByExample(userRoleRelationExample) > 0) {
            projects = extProjectMapper.getAllProjectByOrgId(organizationId);
        } else {
            projects = extProjectMapper.getProjectByOrgId(userId, organizationId);
        }
        if (CollectionUtils.isNotEmpty(projects)) {
            result = projects.stream().map(e -> new OptionDTO(e.getId(), e.getName())).collect(Collectors.toList());
        }
        return result;
    }

    public List<EnvironmentGroupRelation> getEnvironmentGroupRelations(List<String> envGroupIds) {
        if (CollectionUtils.isEmpty(envGroupIds)) {
            return Collections.emptyList();
        }
        EnvironmentGroupRelationExample example = new EnvironmentGroupRelationExample();
        example.createCriteria().andEnvironmentGroupIdIn(envGroupIds);
        return environmentGroupRelationMapper.selectByExample(example);
    }

    @Override
    public void updatePos(String id, long pos) {
        extEnvironmentMapper.updateGroupPos(id, pos);
    }

    @Override
    public void refreshPos(String projectId) {
        List<String> groupIds = extEnvironmentMapper.selectGroupIdByProjectIdOrderByPos(projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtEnvironmentMapper batchUpdateMapper = sqlSession.getMapper(ExtEnvironmentMapper.class);
        for (int i = 0; i < groupIds.size(); i++) {
            batchUpdateMapper.updateGroupPos(groupIds.get(i), i * DEFAULT_NODE_INTERVAL_POS);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    public void moveNode(PosRequest posRequest) {
        NodeMoveRequest request = super.getNodeMoveRequest(posRequest, true);
        MoveNodeSortDTO sortDTO = super.getNodeSortDTO(
                posRequest.getProjectId(),
                request,
                extEnvironmentMapper::selectGroupDragInfoById,
                extEnvironmentMapper::selectGroupNodeByPosOperator
        );
        this.sort(sortDTO);
    }
}
