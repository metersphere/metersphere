package io.metersphere.track.issue;

import io.metersphere.base.domain.Issues;
import io.metersphere.track.issue.domain.PlatformUser;
import io.metersphere.track.request.testcase.IssuesRequest;

import java.util.List;

public interface IssuesPlatform {

    /**
     * 获取平台相关联的缺陷
     * @return platform issues list
     */
    List<Issues> getIssue();

    /**
     * 添加缺陷到缺陷平台
     * @param issuesRequest issueRequest
     */
    void addIssue(IssuesRequest issuesRequest);

    /**
     * 删除缺陷平台缺陷
     * @param id issue id
     */
    void deleteIssue(String id);

    /**
     * 测试平台联通性
     */
    void testAuth();

    /**
     * 获取缺陷平台项目下的相关人员
     * @return platform user list
     */
    List<PlatformUser> getPlatformUser();
}
