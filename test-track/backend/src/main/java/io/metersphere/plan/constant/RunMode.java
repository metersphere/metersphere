package io.metersphere.plan.constant;

import lombok.Getter;

@Getter
public enum RunMode {
    RUN_MODE_SERIAL("serial", "串行"),
    RUN_MODE_PARALLEL("parallel", "并行");;
    private String code;
    private String desc;

    RunMode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
