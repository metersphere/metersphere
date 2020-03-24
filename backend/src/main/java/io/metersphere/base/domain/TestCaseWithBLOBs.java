package io.metersphere.base.domain;

import java.io.Serializable;

public class TestCaseWithBLOBs extends TestCase implements Serializable {
    private String detail;

    private String steps;

    private String tags;

    private static final long serialVersionUID = 1L;

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail == null ? null : detail.trim();
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps == null ? null : steps.trim();
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags == null ? null : tags.trim();
    }
}