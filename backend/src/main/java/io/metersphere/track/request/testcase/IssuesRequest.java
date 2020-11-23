package io.metersphere.track.request.testcase;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IssuesRequest {
    private String title;
    private String content;
    private String projectId;
    private String testCaseId;
    private List<String> tapdUsers;
    /**
     * zentao bug 处理人
     */
    private String zentaoUser;
    /**
     * zentao bug 影响版本
     */
    private List<String> zentaoBuilds;
}
