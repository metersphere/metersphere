package io.metersphere.track.issue;

import io.metersphere.base.domain.IssuesDao;
import io.metersphere.track.dto.DemandDTO;
import io.metersphere.track.issue.domain.PlatformUser;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.request.testcase.IssuesUpdateRequest;

import java.util.List;

public interface IssuesPlatform {

    /**
     * 获取平台相关联的缺陷
     *
     * @return platform issues list
     */
    List<IssuesDao> getIssue(IssuesRequest request);

    /**
     * 过滤分页数据
     * @param issues
     */
    void filter(List<IssuesDao> issues);

    /*获取平台相关需求*/
    List<DemandDTO> getDemandList(String projectId);

    /**
     * 添加缺陷到缺陷平台
     *
     * @param issuesRequest issueRequest
     */
    void addIssue(IssuesUpdateRequest issuesRequest);

    /**
     * 更新缺陷
     * @param request
     */
    void updateIssue(IssuesUpdateRequest request);

    /**
     * 删除缺陷平台缺陷
     *
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
