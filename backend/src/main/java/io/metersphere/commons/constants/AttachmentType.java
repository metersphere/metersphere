package io.metersphere.commons.constants;

public enum AttachmentType {

    TEST_CASE("testcase"), ISSUE("issue");

    // 附件类型名称
    private String type;

    AttachmentType(String type) {
        this.type = type;
    }

    public String type() {
        return this.type;
    }
}
