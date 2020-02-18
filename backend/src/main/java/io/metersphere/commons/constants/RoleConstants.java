package io.metersphere.commons.constants;

public enum RoleConstants {
    ADMIN("admin"), ORGADMIN("org_admin"), TESTUSER("test_user"), TESTVIEWER("test_viewer"), TESTMANAGER("test_manager");
    private String value;

    RoleConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
