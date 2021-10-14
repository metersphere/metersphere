package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.GroupMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.WorkspaceMapper;
import io.metersphere.commons.constants.UserGroupType;
import io.metersphere.dto.OrganizationResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationService {

    @Resource
    private WorkspaceMapper workspaceMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private GroupMapper groupMapper;


    public OrganizationResource listResource(String groupId, String type) {
        Group group = groupMapper.selectByPrimaryKey(groupId);
        String orgId = group.getScopeId();
        OrganizationResource resource = new OrganizationResource();
//        if (!StringUtils.equals("global", orgId)) {
//            Organization organization = organizationMapper.selectByPrimaryKey(orgId);
//            if (organization == null) {
//                return resource;
//            }
//        }

        if (StringUtils.equals(UserGroupType.WORKSPACE, type)) {
            WorkspaceExample workspaceExample = new WorkspaceExample();
            WorkspaceExample.Criteria criteria = workspaceExample.createCriteria();
            if (!StringUtils.equals(orgId, "global")) {
                criteria.andOrganizationIdEqualTo(orgId);
            }
            List<Workspace> workspaces = workspaceMapper.selectByExample(workspaceExample);
            resource.setWorkspaces(workspaces);
        }

        if (StringUtils.equals(UserGroupType.PROJECT, type)) {
            ProjectExample projectExample = new ProjectExample();
            ProjectExample.Criteria pc = projectExample.createCriteria();
            WorkspaceExample workspaceExample = new WorkspaceExample();
            WorkspaceExample.Criteria criteria = workspaceExample.createCriteria();
            if (!StringUtils.equals(orgId, "global")) {
                criteria.andOrganizationIdEqualTo(orgId);
                List<Workspace> workspaces = workspaceMapper.selectByExample(workspaceExample);
                List<String> list = workspaces.stream().map(Workspace::getId).collect(Collectors.toList());
                pc.andWorkspaceIdIn(list);
            }
            List<Project> projects = projectMapper.selectByExample(projectExample);
            resource.setProjects(projects);
        }

        return resource;
    }

}
