package io.metersphere.system.mapper;

import io.metersphere.system.domain.Organization;

import java.util.List;

public interface BaseOrganizationMapper {

    List<Organization> selectOrganizationByIdList(List<String> organizationIds);
}
