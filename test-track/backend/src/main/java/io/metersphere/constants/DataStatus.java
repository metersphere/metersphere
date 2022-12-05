package io.metersphere.constants;

public enum DataStatus {
    UNEXECUTE("UnExecute", "未开始"),
    UNDERWAY("Underway", "进行中"),
    TRASH("Trash", "废弃"),
    PREPARE("Prepare", "未开始"),
    COMPLETED("Completed", "已完成");

    private String value;
    private String desc;

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    DataStatus(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
