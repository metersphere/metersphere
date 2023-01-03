package io.metersphere.constants;

public enum CaseReviewStatus {
    PREPARE("Prepare", "未评审"),
    AGAIN("Again", "重新提审"),
    PASS("Pass", "通过"),
    UNPASS("UnPass", "失败");

    private String value;
    private String desc;

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    CaseReviewStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
