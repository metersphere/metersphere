package io.metersphere.system.service;

import io.metersphere.sdk.constants.InternalUserRole;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.mapper.ExtOrganizationMapper;
import io.metersphere.system.mapper.OrganizationMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.request.OrganizationMemberRequest;
import io.metersphere.system.request.OrganizationRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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

    public void addMember(OrganizationMemberRequest organizationMemberRequest, String createUserId) {
        Organization organization = organizationMapper.selectByPrimaryKey(organizationMemberRequest.getOrganizationId());
        if (organization == null) {
            throw new MSException(Translator.get("organization_not_exist"));
        }
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
        Organization organization = organizationMapper.selectByPrimaryKey(organizationId);
        if (organization == null) {
            throw new MSException(Translator.get("organization_not_exist"));
        }
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            throw new MSException(Translator.get("organization_member_not_exist"));
        }
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(organizationId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(userRoleRelations)) {
            throw new MSException(Translator.get("organization_member_not_exist"));
        }
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
}
