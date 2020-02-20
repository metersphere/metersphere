package io.metersphere.service;

import io.metersphere.base.domain.Organization;
import io.metersphere.base.domain.OrganizationExample;
import io.metersphere.base.mapper.OrganizationMapper;
import io.metersphere.base.mapper.UserRoleMapper;
import io.metersphere.base.mapper.ext.ExtUserRoleMapper;
import io.metersphere.dto.UserRoleHelpDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationService {

    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private ExtUserRoleMapper extUserRoleMapper;

    public Organization addOrganization(Organization organization) {
        long currentTimeMillis = System.currentTimeMillis();
        organization.setId(UUID.randomUUID().toString());
        organization.setCreateTime(currentTimeMillis);
        organization.setUpdateTime(currentTimeMillis);
        organizationMapper.insertSelective(organization);
        return organization;
    }

    public List<Organization> getOrganizationList() {
        return organizationMapper.selectByExample(null);
    }

    public void deleteOrganization(String organizationId) {
        organizationMapper.deleteByPrimaryKey(organizationId);
    }

    public void updateOrganization(Organization organization) {
        organization.setUpdateTime(System.currentTimeMillis());
        organizationMapper.updateByPrimaryKeySelective(organization);
    }

    public List<Organization> getOrganizationListByUserId(String userId) {
        List<UserRoleHelpDTO> userRoleHelpList = extUserRoleMapper.getUserRoleHelpList(userId);
        List<String> list = new ArrayList<>();
        userRoleHelpList.forEach(r -> {
            if (StringUtils.isEmpty(r.getParentId())) {
                list.add(r.getSourceId());
            } else {
                list.add(r.getParentId());
            }
        });
        OrganizationExample organizationExample = new OrganizationExample();
        organizationExample.createCriteria().andIdIn(list);
        return organizationMapper.selectByExample(organizationExample);
    }
}
