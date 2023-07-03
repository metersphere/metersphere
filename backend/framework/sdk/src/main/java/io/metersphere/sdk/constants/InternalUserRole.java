package io.metersphere.sdk.constants;

/**
 * 系统内置用户组ID
 * @author jianxing
 */
public enum InternalUserRole {

    ADMIN("admin"),
    MEMBER("member"),
    ORG_ADMIN("org_admin"),
    ORG_MEMBER("org_member"),
    PROJECT_ADMIN("project_admin"),
    PROJECT_MEMBER("project_member");

    private String value;

    InternalUserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
