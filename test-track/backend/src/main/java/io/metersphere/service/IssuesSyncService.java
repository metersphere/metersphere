package io.metersphere.service;

import io.metersphere.base.domain.Project;
import io.metersphere.base.mapper.ext.ExtIssuesMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.xpack.track.dto.IssueSyncRequest;
import io.metersphere.xpack.track.dto.IssuesDao;
import io.metersphere.xpack.track.dto.request.IssuesRequest;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author song-cc-rock
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class IssuesSyncService {

    @Resource
    private IssuesService issuesService;
    @Resource
    private BaseProjectService baseProjectService;
    @Resource
    private ExtIssuesMapper extIssuesMapper;

    /**
     * xpack用户
     * @param syncRequest 同步参数
     */
    public void syncAllIssues(IssueSyncRequest syncRequest) {
        try {
            if (StringUtils.isNotBlank(syncRequest.getProjectId())) {
                // 获取当前项目执行同步缺陷Key
                String syncValue = issuesService.getSyncKey(syncRequest.getProjectId());
                if (StringUtils.isEmpty(syncValue)) {
                    // 同步Key不存在, 设置保证唯一性, 并开始同步
                    issuesService.setSyncKey(syncRequest.getProjectId());
                    Project project = baseProjectService.getProjectById(syncRequest.getProjectId());
                    if (!issuesService.isThirdPartTemplate(project)) {
                        syncRequest.setDefaultCustomFields(issuesService.getDefaultCustomFields(syncRequest.getProjectId()));
                    }
                    issuesService.syncThirdPartyAllIssues(syncRequest, project);
                }
            }
        } catch (Exception e) {
            issuesService.deleteSyncKey(syncRequest.getProjectId());
            MSException.throwException(e);
        }
    }

    /**
     * 非xpack用户
     * @param projectId 项目ID
     */
    public void syncIssues(String projectId) {
        try {
            if (StringUtils.isNotBlank(projectId)) {
                String syncValue = issuesService.getSyncKey(projectId);
                if (StringUtils.isEmpty(syncValue)) {
                    issuesService.setSyncKey(projectId);
                    Project project = baseProjectService.getProjectById(projectId);
                    List<IssuesDao> issues = extIssuesMapper.getIssueForSync(projectId, project.getPlatform());

                    if (CollectionUtils.isEmpty(issues)) {
                        issuesService.deleteSyncKey(projectId);
                    } else {
                        IssuesRequest issuesRequest = new IssuesRequest();
                        issuesRequest.setProjectId(projectId);
                        issuesRequest.setWorkspaceId(project.getWorkspaceId());
                        issuesRequest.setDefaultCustomFields(issuesService.getDefaultCustomField(project));
                        issuesService.syncThirdPartyIssues(issues, issuesRequest, project);
                    }
                }
            }
        } catch (Exception e) {
            issuesService.deleteSyncKey(projectId);
            MSException.throwException(e);
        }
    }
}
