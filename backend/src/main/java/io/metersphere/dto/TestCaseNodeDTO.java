package io.metersphere.dto;

import io.metersphere.base.domain.TestCaseNode;

import java.util.List;

public class TestCaseNodeDTO extends TestCaseNode {

    private String label;
    private List<TestCaseNodeDTO> children;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<TestCaseNodeDTO> getChildren() {
        return children;
    }

    public void setChildren(List<TestCaseNodeDTO> children) {
        this.children = children;
    }
}
