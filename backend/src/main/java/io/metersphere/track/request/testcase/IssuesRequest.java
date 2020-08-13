package io.metersphere.track.request.testcase;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssuesRequest {
    private String title;
    private String content;
    private String projectId;
    private String testCaseId;
}
