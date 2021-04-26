package io.metersphere.track.request.testcase;

import io.metersphere.base.domain.IssuesWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IssuesUpdateRequest extends IssuesWithBLOBs {
    private String content;
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
    private List<String> testCaseIds;
}
