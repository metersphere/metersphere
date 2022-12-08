package io.metersphere.xpack.track.issue;

import io.metersphere.base.domain.IssuesWithBLOBs;
import io.metersphere.base.domain.Project;
import io.metersphere.dto.UserDTO;
import io.metersphere.xpack.track.dto.*;
import io.metersphere.xpack.track.dto.request.IssuesRequest;
import io.metersphere.xpack.track.dto.request.IssuesUpdateRequest;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.List;

public interface IssuesPlatform {

    /**
     * 获取平台相关联的缺陷
     *
     * @return platform issues list
     */
    List<IssuesDao> getIssue(IssuesRequest request);

    /*获取平台相关需求*/
    List<DemandDTO> getDemandList(String projectId);

    /**
     * 添加缺陷到缺陷平台
     *
     * @param issuesRequest issueRequest
     */
    IssuesWithBLOBs addIssue(IssuesUpdateRequest issuesRequest);

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
     * 用户信息测试
     */
    void userAuth(UserDTO.PlatformInfo userInfo);

    /**
     * 获取缺陷平台项目下的相关人员
     * @return platform user list
     */
    List<PlatformUser> getPlatformUser();

    /**
     * 同步缺陷最新变更
     * @param project
     * @param tapdIssues
     */
    void syncIssues(Project project, List<IssuesDao> tapdIssues);

    /**
     * 同步缺陷全量的缺陷
     * @param project
     */
    void syncAllIssues(Project project, IssueSyncRequest syncRequest);

    /**
     * 获取第三方平台缺陷模板
     * @return
     */
    IssueTemplateDao getThirdPartTemplate();

    /**
     * 检查其它平台关联的ID是否存在
     * @param relateId 其它平台在MS项目上关联的相关ID
     * @return Boolean
     */
    Boolean checkProjectExist(String relateId);

    /**
     * 更新缺陷关联关系
     * @param request
     */
    void removeIssueParentLink(IssuesUpdateRequest request);

    /**
     * 更新需求与缺陷关联关系
     *
     * @param testCase
     */
    void updateDemandIssueLink(EditTestCaseRequest testCase, Project project);

    /**
     * @param request
     * @param type    add or edit
     */
    void updateDemandHyperLink(EditTestCaseRequest request, Project project, String type);

    /**
     * Get请求的代理
     * @param path
     * @return
     */
    ResponseEntity proxyForGet(String path, Class responseEntityClazz);

    /**
     * 同步MS缺陷附件到第三方平台
     * @param issuesRequest 平台参数
     * @param file 附件
     * @param syncType 同步操作类型: UPLOAD, DELETE
     */
    void syncIssuesAttachment(IssuesUpdateRequest issuesRequest, File file, AttachmentSyncType syncType);


    /**
     * 获取第三方平台的状态集合
     * @param issueKey
     * @return
     */
    List<PlatformStatusDTO> getTransitions(String issueKey);
}
