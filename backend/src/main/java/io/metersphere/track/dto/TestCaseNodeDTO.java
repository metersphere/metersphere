package io.metersphere.track.dto;

import io.metersphere.base.domain.TestCaseNode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestCaseNodeDTO extends TestCaseNode {

    private String label;
    private List<TestCaseNodeDTO> children;

}
