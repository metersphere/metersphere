package io.metersphere.track.request.testcase;

import io.metersphere.base.domain.TestCaseWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TestCaseMinderEditRequest {
    private String projectId;
    private List<String> ids;
    List<TestCaseMinderEditItem> data;

    @Getter
    @Setter
    public static class TestCaseMinderEditItem extends TestCaseWithBLOBs {
        private String targetId;
        private String moveMode;
    }
}
