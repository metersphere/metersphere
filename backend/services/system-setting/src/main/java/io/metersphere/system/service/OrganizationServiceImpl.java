package io.metersphere.system.service;

import io.metersphere.sdk.constants.UserRoleConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.domain.OrganizationExample;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.dto.OrganizationDTO;
import io.metersphere.system.dto.UserExtend;
import io.metersphere.system.mapper.ExtOrganizationMapper;
import io.metersphere.system.mapper.OrganizationMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.request.OrganizationMemberRequest;
import io.metersphere.system.request.OrganizationRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author song-cc-rock
 * 组织功能(非XPACK)
 */
@Service
public class OrganizationServiceImpl implements OrganizationService{

    @Resource
    OrganizationMapper organizationMapper;
    @Resource
    ExtOrganizationMapper extOrganizationMapper;
    @Resource
    UserRoleRelationMapper userRoleRelationMapper;


    @Override
    public List<OrganizationDTO> list(OrganizationRequest organizationRequest) {
        return extOrganizationMapper.list(organizationRequest);
    }

    @Override
    public OrganizationDTO getDefault() {
        OrganizationDTO organizationDTO = new OrganizationDTO();
        OrganizationExample example = new OrganizationExample();
        example.createCriteria().andNumEqualTo(100001L);
        List<Organization> organizations = organizationMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(organizations)) {
            throw new MSException(Translator.get("organization_default_not_exists"));
        }
        Organization organization = organizations.get(0);
        BeanUtils.copyBean(organizationDTO, organization);
        return organizationDTO;
    }

    @Override
    public List<UserExtend> listMember(OrganizationRequest organizationRequest) {
        return extOrganizationMapper.listMember(organizationRequest);
    }

    @Override
    public void addMember(OrganizationMemberRequest organizationMemberRequest) {
        if (CollectionUtils.isEmpty(organizationMemberRequest.getMemberIds())) {
            return;
        }
        for (String userId : organizationMemberRequest.getMemberIds()) {
            UserRoleRelation userRoleRelation = new UserRoleRelation();
            userRoleRelation.setId(UUID.randomUUID().toString());
            userRoleRelation.setUserId(userId);
            userRoleRelation.setSourceId(organizationMemberRequest.getOrganizationId());
            userRoleRelation.setRoleId(UserRoleConstants.ORG_MEMBER);
            userRoleRelation.setCreateTime(System.currentTimeMillis());
            userRoleRelation.setCreateUser(organizationMemberRequest.getCreateUserId());
            userRoleRelationMapper.insertSelective(userRoleRelation);
        }
    }

    @Override
    public void removeMember(String organizationId, String userId) {
        UserRoleRelationExample example = new UserRoleRelationExample();
        example.createCriteria().andUserIdEqualTo(userId).andSourceIdEqualTo(organizationId);
        userRoleRelationMapper.deleteByExample(example);
    }
}
