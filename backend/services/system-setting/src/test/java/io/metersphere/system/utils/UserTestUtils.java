package io.metersphere.system.utils;

import io.metersphere.sdk.dto.BasePageRequest;
import io.metersphere.system.domain.Organization;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRole;
import io.metersphere.system.dto.UserBatchCreateDTO;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

public class UserTestUtils {

    public static UserBatchCreateDTO getSimpleUserCreateDTO(List<Organization> organizationList,
                                                            List<UserRole> userRoleList,
                                                            List<User> userInfoList) {
        UserBatchCreateDTO userMaintainRequest = new UserBatchCreateDTO();
        if (CollectionUtils.isNotEmpty(organizationList)) {
            userMaintainRequest.setOrganizationIdList(
                    organizationList.stream().map(Organization::getId).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(userRoleList)) {
            userMaintainRequest.setUserRoleIdList(
                    userRoleList.stream().map(UserRole::getId).collect(Collectors.toList()));
        }
        userMaintainRequest.setUserInfoList(userInfoList);
        return userMaintainRequest;
    }

    public static BasePageRequest getDefaultPageRequest() {
        return new BasePageRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
        }};
    }
}
