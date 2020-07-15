package io.metersphere.track.request.testcase;

import io.metersphere.base.domain.TestCaseNode;
import io.metersphere.track.dto.TestCaseNodeDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DragNodeRequest extends TestCaseNode {

    List<String> nodeIds;
    TestCaseNodeDTO nodeTree;
}
