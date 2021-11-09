package io.metersphere.track.request.testcase;

import io.metersphere.base.domain.MinderExtraNode;
import io.metersphere.base.domain.TestCaseNode;
import io.metersphere.base.domain.TestCaseWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TestCaseMinderEditRequest {
    private String projectId;
    private List<String> ids;
    private List<TestCaseMinderEditItem> data;
    private List<TestCaseNodeMinderEditItem> testCaseNodes;

    private MinderExtraNodeEditRequest extraNodeRequest;

    @Getter
    @Setter
    public static class MinderExtraNodeEditRequest extends MinderExtraNode {
        private Map<String, List<String>> data;
    }

    @Getter
    @Setter
    public static class TestCaseMinderEditItem extends TestCaseWithBLOBs {
        private Boolean isEdit;
        private String targetId;
        private String moveMode;
    }

    @Getter
    @Setter
    public static class TestCaseNodeMinderEditItem extends TestCaseNode {
        private Boolean isEdit;
        private List<String> nodeIds;
    }
}
