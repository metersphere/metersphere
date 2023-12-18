package io.metersphere.request.testplan;

import io.metersphere.base.domain.TestPlanNode;
import io.metersphere.dto.TestPlanNodeDTO;
import lombok.Data;

import java.io.Serial;
import java.util.List;

/**
 * @author song-cc-rock
 */
@Data
public class DragPlanNodeRequest extends TestPlanNode {

    @Serial
    private static final long serialVersionUID = -2663513817971996721L;

    private List<String> nodeIds;
    private TestPlanNodeDTO nodeTree;
}
