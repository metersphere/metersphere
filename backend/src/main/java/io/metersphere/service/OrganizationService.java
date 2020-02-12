package io.metersphere.service;

import io.metersphere.base.domain.Organization;
import io.metersphere.base.mapper.OrganizationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationService {

    @Resource
    private OrganizationMapper organizationMapper;

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
}
