package io.metersphere.system.utils;

import io.metersphere.system.domain.User;
import io.metersphere.system.dto.UserMaintainRequest;

import java.util.ArrayList;

public class UserTestUtils {

    public static final String USER_DEFAULT_NAME = "tianyang.no.1";
    public static final String USER_DEFAULT_EMAIL = "tianyang.no.1@126.com";
    public static final String ORGANIZATION_ID = "ms-organization";
    public static final String PROJECT_ID = "ms-project";

    public static UserMaintainRequest getSimpleUserCreateDTO() {
        UserMaintainRequest userMaintainRequest = new UserMaintainRequest();
        userMaintainRequest.setOrganizationId(ORGANIZATION_ID);
        userMaintainRequest.setProjectId(PROJECT_ID);
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

    public static UserMaintainRequest getErrorUserCreateDTO(boolean organizationIsEmpty, boolean projectIsEmpty, boolean userIsEmpty) {
        UserMaintainRequest userMaintainRequest = new UserMaintainRequest();
        if (!organizationIsEmpty) {
            userMaintainRequest.setOrganizationId(ORGANIZATION_ID);
        }
        if (!projectIsEmpty) {
            userMaintainRequest.setProjectId(PROJECT_ID);
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
}
