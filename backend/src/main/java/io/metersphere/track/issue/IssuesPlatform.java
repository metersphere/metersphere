package io.metersphere.track.issue;

import io.metersphere.base.domain.Issues;
import io.metersphere.track.request.testcase.IssuesRequest;

import java.util.List;

public interface IssuesPlatform {

    /**
     * 获取平台相关联的缺陷
     * @return
     */
    List<Issues> getIssue();

    /**
     * 添加缺陷到缺陷平台
     * @param issuesRequest
     */
    void addIssue(IssuesRequest issuesRequest);

    /**
     * 删除缺陷平台缺陷
     * @param id
     */
    void deleteIssue(String id);

    /**
     * 测试缺陷平台连通性
     * @param
     */
    void testAuth();

    /**
     * 获取缺陷平台项目下的相关人员
     * @return
     */
    List<PlatformUser> getPlatformUser();
}
