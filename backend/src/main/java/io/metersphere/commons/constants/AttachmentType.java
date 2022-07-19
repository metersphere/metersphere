package io.metersphere.commons.constants;

/**
 * @author songcc
 */

public enum AttachmentType {

    /**
     * 测试用例类型
     */
    TEST_CASE("testcase"),

    /**
     * 缺陷类型
     */
    ISSUE("issue");

    private String type;

    AttachmentType(String type) {
        this.type = type;
    }

    public String type() {
        return this.type;
    }
}
