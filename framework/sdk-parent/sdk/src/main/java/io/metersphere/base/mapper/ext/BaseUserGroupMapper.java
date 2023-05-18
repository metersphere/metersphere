package io.metersphere.base.mapper.ext;

import io.metersphere.base.domain.Group;
import io.metersphere.base.domain.User;
import io.metersphere.dto.RelatedSource;
import io.metersphere.dto.UserGroupDTO;
import io.metersphere.dto.UserGroupInfoDTO;
import io.metersphere.request.group.EditGroupRequest;
import io.metersphere.request.member.QueryMemberRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseUserGroupMapper {

    List<UserGroupDTO> getUserGroup(@Param("userId") String userId, @Param("projectId") String projectId);

    List<Group> getWorkspaceMemberGroups(@Param("workspaceId") String workspaceId, @Param("userId") String userId);

    List<User> getMemberList(@Param("member") QueryMemberRequest request);

    List<User> getProjectMemberList(@Param("request") QueryMemberRequest request);

    List<Group> getProjectMemberGroups(@Param("projectId") String projectId, @Param("userId") String userId);

    List<RelatedSource> getRelatedSource(@Param("userId") String userId);

    List<User> getGroupUser(@Param("request") EditGroupRequest request);

    int checkSourceRole(@Param("sourceId") String sourceId, @Param("userId") String userId, @Param("groupId") String groupId);

    List<UserGroupInfoDTO> getUserGroupInfoByProjectId(String projectId);

    List<User> getProjectMemberOption(@Param("projectId") String projectId);

    List<String> getProjectUserId(String projectId);
}
