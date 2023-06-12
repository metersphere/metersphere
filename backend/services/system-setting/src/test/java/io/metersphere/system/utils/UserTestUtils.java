package io.metersphere.system.utils;

import io.metersphere.sdk.dto.BasePageRequest;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.UserBatchCreateDTO;

import java.util.ArrayList;

public class UserTestUtils {

    public static final String USER_DEFAULT_NAME = "tianyang.no.1";
    public static final String USER_DEFAULT_EMAIL = "tianyang.no.1@126.com";
    public static final String ORGANIZATION_ID = "ms-organization";
    public static final String USER_ROLE_ID = "ms-user-role";

    public static UserBatchCreateDTO getSimpleUserCreateDTO() {
        UserBatchCreateDTO userMaintainRequest = new UserBatchCreateDTO();
        userMaintainRequest.setOrganizationIdList(new ArrayList<>() {{
            add(ORGANIZATION_ID);
        }});
        userMaintainRequest.setUserRoleIdList(new ArrayList<>() {{
            add(USER_ROLE_ID);
        }});
        userMaintainRequest.setUserInfoList(new ArrayList<>() {{
            add(new User() {{
                setName(USER_DEFAULT_NAME);
                setEmail(USER_DEFAULT_EMAIL);
                setSource("LOCAL");
            }});
            add(new User() {{
                setName("tianyang.no.2");
                setEmail("tianyang.no.2@126.com");
                setSource("LOCAL");
            }});
        }});
        return userMaintainRequest;
    }

    public static UserBatchCreateDTO getErrorUserCreateDTO(boolean organizationIsEmpty, boolean roleIsEmpty, boolean userIsEmpty) {
        UserBatchCreateDTO userMaintainRequest = new UserBatchCreateDTO();
        if (!organizationIsEmpty) {
            userMaintainRequest.setOrganizationIdList(new ArrayList<>() {{
                add(ORGANIZATION_ID);
            }});
        }
        if (!roleIsEmpty) {
            userMaintainRequest.setUserRoleIdList(new ArrayList<>() {{
                add(USER_ROLE_ID);
            }});
        }
        if (!userIsEmpty) {
            userMaintainRequest.setUserInfoList(new ArrayList<>() {{
                add(new User() {{
                    setName("tianyang.error.1");
                    setEmail("tianyang.error.1@126.com");
                    setSource("LOCAL");
                }});
                add(new User() {{
                    setName("tianyang.error.2");
                    setEmail("tianyang.error.2@126.com");
                    setSource("LOCAL");
                }});
            }});
        }
        return userMaintainRequest;
    }

    public static BasePageRequest getDefaultPageRequest() {
        return new BasePageRequest() {{
            this.setCurrent(1);
            this.setPageSize(10);
        }};
    }
}
