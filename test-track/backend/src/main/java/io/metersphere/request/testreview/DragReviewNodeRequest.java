package io.metersphere.request.testreview;

import io.metersphere.base.domain.TestCaseReviewNode;
import io.metersphere.dto.TestCaseReviewNodeDTO;
import lombok.Data;

import java.io.Serial;
import java.util.List;

/**
 * @author song-cc-rock
 */
@Data
public class DragReviewNodeRequest extends TestCaseReviewNode {

    @Serial
    private static final long serialVersionUID = -2663513817971996721L;

    private List<String> nodeIds;
    private TestCaseReviewNodeDTO nodeTree;
}
