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
    private String workspaceId;
    // 带caseId的更新, 默认更新是同时保存用例的关联关系，脑图保存缺陷，不带caseId
    private boolean withCaseId = true;

    private List<String> tapdUsers;
    /**
     * zentao bug 处理人
     */
    private String zentaoUser;
    private String zentaoAssigned;
    /**
     * zentao bug 影响版本
     */
    private List<String> zentaoBuilds;
    private List<String> testCaseIds;

    private List<String> follows;
}
