package io.metersphere.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.GroupMapper;
import io.metersphere.base.mapper.ext.ExtGroupMapper;
import io.metersphere.base.mapper.ext.ExtUserGroupMapper;
import io.metersphere.commons.constants.UserGroupType;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.PageUtils;
import io.metersphere.commons.utils.Pager;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.group.EditGroupRequest;
import io.metersphere.dto.GroupDTO;
import io.metersphere.dto.UserGroupDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class GroupService {


    @Resource
    private ExtUserGroupMapper extUserGroupMapper;
    @Resource
    private GroupMapper groupMapper;
    @Resource
    private ExtGroupMapper extGroupMapper;

    public Pager<List<GroupDTO>> getGroupList(EditGroupRequest request) {
        SessionUser user = SessionUtils.getUser();
        List<UserGroupDTO> userGroup = extUserGroupMapper.getUserGroup(user.getId());
        List<String> groupTypeList = userGroup.stream().map(UserGroupDTO::getType).collect(Collectors.toList());
        return getGroups(groupTypeList, request);
    }

    public Group addGroup(EditGroupRequest request) {
        Group group = new Group();
        group.setId(UUID.randomUUID().toString());
        group.setName(request.getName());
        group.setCreator(SessionUtils.getUserId());
        group.setDescription(request.getDescription());
        group.setSystem(false);
        group.setType(request.getType());
        group.setCreateTime(System.currentTimeMillis());
        group.setUpdateTime(System.currentTimeMillis());
        if (BooleanUtils.isTrue(request.getGlobal())) {
            group.setScopeId("global");
        } else {
            group.setScopeId(request.getScopeId());
        }
        groupMapper.insertSelective(group);
        return group;
    }

    public void editGroup(EditGroupRequest request) {
        Group group = new Group();
        request.setScopeId(null);
        BeanUtils.copyBean(group, request);
        group.setCreateTime(System.currentTimeMillis());
        groupMapper.updateByPrimaryKeySelective(group);
    }

    public void deleteGroup(String id) {
        groupMapper.deleteByPrimaryKey(id);
        // todo use_group 关系
    }




    private Pager<List<GroupDTO>> getGroups(List<String> groupTypeList, EditGroupRequest request) {
        if (groupTypeList.contains(UserGroupType.SYSTEM)) {
            return getUserGroup(UserGroupType.SYSTEM, request);
        }

        if (groupTypeList.contains(UserGroupType.ORGANIZATION)) {
            return getUserGroup(UserGroupType.ORGANIZATION, request);
        }

        if (groupTypeList.contains(UserGroupType.WORKSPACE)) {
            return getUserGroup(UserGroupType.WORKSPACE, request);
        }

        if (groupTypeList.contains(UserGroupType.PROJECT)) {
            return getUserGroup(UserGroupType.PROJECT, request);
        }

        return new Pager<>();
    }


    private Pager<List<GroupDTO>> getUserGroup(String groupType, EditGroupRequest request) {
        List<String> types = new ArrayList<>();
        String orgId = SessionUtils.getCurrentOrganizationId();
        List<String> scopes = Arrays.asList("global", orgId);
        int goPage = request.getGoPage();
        int pageSize = request.getPageSize();
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        switch (groupType) {
            case UserGroupType.SYSTEM:
                types = Arrays.asList(UserGroupType.SYSTEM,UserGroupType.ORGANIZATION, UserGroupType.WORKSPACE, UserGroupType.PROJECT);
                scopes = new ArrayList<>();
                break;
            case UserGroupType.ORGANIZATION:
                types = Arrays.asList(UserGroupType.ORGANIZATION, UserGroupType.WORKSPACE, UserGroupType.PROJECT);
                break;
            case UserGroupType.WORKSPACE:
                types = Arrays.asList(UserGroupType.WORKSPACE, UserGroupType.PROJECT);
                break;
            case UserGroupType.PROJECT:
                types.add(UserGroupType.PROJECT);
                break;
            default:
        }
        request.setTypes(types);
        request.setScopes(scopes);
        List<GroupDTO> groups = extGroupMapper.getGroupList(request);
        return PageUtils.setPageInfo(page, groups);
    }


}
