package io.metersphere.system.service;

import com.dingtalk.api.response.OapiV2UserGetResponse;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionExample;
import io.metersphere.constants.HttpMethodConstants;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.dto.LogDTO;
import io.metersphere.sdk.dto.TestResourcePoolRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.log.constants.OperationLogModule;
import io.metersphere.sdk.log.constants.OperationLogType;
import io.metersphere.sdk.log.service.OperationLogService;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.OrgUserExtend;
import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.dto.OrganizationProjectOptionsDto;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.mapper.*;
import io.metersphere.system.request.OrganizationMemberExtendRequest;
import io.metersphere.system.request.OrganizationMemberRequest;
import io.metersphere.system.request.OrganizationRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author song-cc-rock
 * 组织功能
 */

@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationService{

    @Resource
    OrganizationMapper organizationMapper;
    @Resource
    ExtOrganizationMapper extOrganizationMapper;
    @Resource
    UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    UserMapper userMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private ProjectMapper projectMapper;

    public List<OrganizationDTO> list(OrganizationRequest organizationRequest) {
        List<OrganizationDTO> organizationDTOS = extOrganizationMapper.list(organizationRequest);
        return buildOrgAdminInfo(organizationDTOS);
    }

    public List<OrganizationDTO> listAll() {
        return extOrganizationMapper.listAll();
    }

    public OrganizationDTO getDefault() {
        OrganizationDTO organizationDTO = new OrganizationDTO();
        OrganizationExample example = new OrganizationExample();
        example.createCriteria().andNumEqualTo(100001L);
        List<Organization> organizations = organizationMapper.selectByExample(example);
        Organization organization = organizations.get(0);
        BeanUtils.copyBean(organizationDTO, organization);
        return organizationDTO;
    }

    public List<UserExtend> listMember(OrganizationRequest organizationRequest) {
        return extOrganizationMapper.listMember(organizationRequest);
    }


    public List<OrgUserExtend> getMemberList(OrganizationRequest organizationRequest) {
        //根据组织ID获取所有组织用户关系表
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andSourceIdEqualTo(organizationRequest.getOrganizationId());
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        List<String> userIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).toList();
        //根据用户ID获取所有用户
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIds);
        List<User> users = userMapper.selectByExample(userExample);
        Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        //根据用户id获取所有与该用户有关的组织关系表
        userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdIn(userIds);
        List<UserRoleRelation> userRoleRelationsByUsers = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        //根据关系表查询出用户的关联组织和用户组
        Map<String, String> projectIdNameMap = new HashMap<>();
        Map<String, String> userRoleIdNameMap = new HashMap<>();
        Map<String, OrgUserExtend> userIdExtendMap = new HashMap<>();
        for (UserRoleRelation userRoleRelationsByUser : userRoleRelationsByUsers) {
            OrgUserExtend userExtend = new OrgUserExtend();
            String projectId = userRoleRelationsByUser.getSourceId();
            String roleId = userRoleRelationsByUser.getRoleId();
            String userId = userRoleRelationsByUser.getUserId();
            if (StringUtils.isBlank(projectIdNameMap.get(projectId))) {
                Project project = projectMapper.selectByPrimaryKey(projectId);
                if (project != null) {
                    projectIdNameMap.put(projectId,project.getName());
                }
            }
            if (StringUtils.isBlank(userRoleIdNameMap.get(roleId))) {
                UserRole userRole = userRoleMapper.selectByPrimaryKey(roleId);
                if (userRole != null) {
                    userRoleIdNameMap.put(roleId,userRole.getName());
                }
            }
            if (userIdExtendMap.get(userId) == null) {
                User user = userMap.get(userId);
                BeanUtils.copyBean(userExtend, user);
                userExtend.setUserRoleIdNameMap(userRoleIdNameMap);
                userExtend.setProjectIdNameMap(projectIdNameMap);
            } else {
                userIdExtendMap.get(userId).setUserRoleIdNameMap(userRoleIdNameMap);
                userIdExtendMap.get(userId).setProjectIdNameMap(projectIdNameMap);
            }
        }
        return new ArrayList<>(userIdExtendMap.values());
    }

    public void addMember(OrganizationMemberRequest organizationMemberRequest, String createUserId) {
        checkOrgExist(organizationMemberRequest.getOrganizationId());
        for (String userId : organizationMemberRequest.getMemberIds()) {
            UserRoleRelation userRoleRelation = new UserRoleRelation();
            userRoleRelation.setId(UUID.randomUUID().toString());
            userRoleRelation.setUserId(userId);
            userRoleRelation.setSourceId(organizationMemberRequest.getOrganizationId());
            userRoleRelation.setRoleId(InternalUserRole.ORG_MEMBER.getValue());
            userRoleRelation.setCreateTime(System.currentTimeMillis());
            userRoleRelation.setCreateUser(createUserId);
            userRoleRelationMapper.insertSelective(userRoleRelation);
        }
    }

    public void removeMember(String organizationId, String userId) {
        checkOrgExist(organizationId);
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new MSException(Translator.get("organization_member_not_exist"));
        }
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(organizationId);
        userRoleRelationMapper.deleteByExample(example);
    }

    private List<OrganizationDTO> buildOrgAdminInfo(List<OrganizationDTO> organizationDTOS) {
        if (CollectionUtils.isEmpty(organizationDTOS)) {
            return organizationDTOS;
        }
        organizationDTOS.forEach(organizationDTO -> {
            List<User> orgAdminList = extOrganizationMapper.getOrgAdminList(organizationDTO.getId());
            organizationDTO.setOrgAdmins(orgAdminList);
        });
        return organizationDTOS;
    }

    public List<OrganizationProjectOptionsDto> getOrganizationOptions() {
        return extOrganizationMapper.selectOrganizationOptions();
    }

    public void addMemberByList(OrganizationMemberExtendRequest organizationMemberExtendRequest, String createUserId) {
        checkOrgExist(organizationMemberExtendRequest.getOrganizationId());
        setRelationByMemberAndGroupIds(organizationMemberExtendRequest, createUserId, true);
    }

    private void setRelationByMemberAndGroupIds(OrganizationMemberExtendRequest organizationMemberExtendRequest, String createUserId, boolean add) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        UserRoleRelationMapper userRoleRelationMapper = sqlSession.getMapper(UserRoleRelationMapper.class);
        List<LogDTO>logDTOList = new ArrayList<>();
        organizationMemberExtendRequest.getMemberIds().forEach(memberId->{
            organizationMemberExtendRequest.getUserGroupIds().forEach(userGroupId->{
                UserRoleRelation userRoleRelation = new UserRoleRelation();
                userRoleRelation.setId(UUID.randomUUID().toString());
                userRoleRelation.setUserId(memberId);
                userRoleRelation.setSourceId(organizationMemberExtendRequest.getOrganizationId());
                userRoleRelation.setRoleId(userGroupId);
                userRoleRelation.setCreateTime(System.currentTimeMillis());
                userRoleRelation.setCreateUser(createUserId);
                userRoleRelationMapper.insertSelective(userRoleRelation);
                //add Log
                setLog(organizationMemberExtendRequest, logDTOList, memberId, userRoleRelation, add);
            });
        });
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        //写入操作日志
        operationLogService.batchAdd(logDTOList);
    }

    private static void setLog(OrganizationMemberExtendRequest organizationMemberExtendRequest, List<LogDTO> logDTOList, String memberId, UserRoleRelation userRoleRelation, boolean add) {
        String type =  add ? OperationLogType.ADD.name() : OperationLogType.UPDATE.name();
        LogDTO dto = new LogDTO(
                "system",
                organizationMemberExtendRequest.getOrganizationId(),
                memberId,
                null,
                type,
                OperationLogModule.ORGANIZATION_MEMBER,
                "成员");
        dto.setPath("/organization/list/add-member");
        dto.setMethod(HttpMethodConstants.POST.name());
        dto.setOriginalValue(JSON.toJSONBytes(userRoleRelation));
        logDTOList.add(dto);
    }

    public void updateMember(OrganizationMemberExtendRequest organizationMemberExtendRequest, String userId) {
        String organizationId = organizationMemberExtendRequest.getOrganizationId();
        checkOrgExist(organizationId);
        //删除旧的关系
        organizationMapper.deleteByPrimaryKey(organizationId);
        //在新增组织成员与用户组和组织的关系
        setRelationByMemberAndGroupIds(organizationMemberExtendRequest, userId, false);
    }

    private void checkOrgExist(String organizationId) {
        Organization organization = organizationMapper.selectByPrimaryKey(organizationId);
        if (organization == null) {
            throw new MSException(Translator.get("organization_not_exist"));
        }
    }
}
