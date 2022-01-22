package io.metersphere.track.request.testcase;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestCaseImportRequest {
    private String projectId;
    private String userId;
    private String importType;
    private String version;
    private boolean ignore;
}
