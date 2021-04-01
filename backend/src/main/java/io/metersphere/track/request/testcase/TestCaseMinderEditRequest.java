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
    List<TestCaseWithBLOBs> data;
}
