package io.metersphere.base.domain;

import java.io.Serializable;

public class TestCaseWithBLOBs extends TestCase implements Serializable {
    private String remark;

    private String steps;

    private static final long serialVersionUID = 1L;

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps == null ? null : steps.trim();
    }
}