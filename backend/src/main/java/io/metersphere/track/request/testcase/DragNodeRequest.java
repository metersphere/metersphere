package io.metersphere.track.request.testcase;

import io.metersphere.base.domain.TestCaseNode;
import io.metersphere.track.dto.TestCaseNodeDTO;
import lombok.Data;

import java.util.List;

@Data
public class DragNodeRequest extends TestCaseNode {

    List<String> nodeIds;
    TestCaseNodeDTO nodeTree;
}
