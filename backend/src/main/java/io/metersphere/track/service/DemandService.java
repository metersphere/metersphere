package io.metersphere.track.service;

import io.metersphere.base.domain.Project;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.track.dto.DemandDTO;
import io.metersphere.track.issue.AbstractIssuePlatform;
import io.metersphere.track.issue.IssueFactory;
import io.metersphere.track.request.testcase.IssuesRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class DemandService {
    @Resource
    private IssuesService issuesService;
    @Resource
    private ProjectMapper projectMapper;

    public List<DemandDTO> getDemandList(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);

        String workspaceId = project.getWorkspaceId();
        boolean tapd = issuesService.isIntegratedPlatform(workspaceId, IssuesManagePlatform.Tapd.toString());
        boolean jira = issuesService.isIntegratedPlatform(workspaceId, IssuesManagePlatform.Jira.toString());
        boolean zentao = issuesService.isIntegratedPlatform(workspaceId, IssuesManagePlatform.Zentao.toString());
        boolean azureDevops = issuesService.isIntegratedPlatform(workspaceId, IssuesManagePlatform.AzureDevops.toString());
        List<DemandDTO> list = new ArrayList<>();
        List<String> platforms = new ArrayList<>();
        IssuesRequest issueRequest = new IssuesRequest();
        if (tapd) {
            // 是否关联了项目
            String tapdId = project.getTapdId();
            if (StringUtils.isNotBlank(tapdId)) {
                platforms.add(IssuesManagePlatform.Tapd.name());
            }
        }

        if (jira) {
            String jiraKey = project.getJiraKey();
            if (StringUtils.isNotBlank(jiraKey)) {
                platforms.add(IssuesManagePlatform.Jira.name());
            }
        }

        if (zentao) {
            String zentaoId = project.getZentaoId();
            if (StringUtils.isNotBlank(zentaoId)) {
                platforms.add(IssuesManagePlatform.Zentao.name());
            }
        }

        if (azureDevops) {
            String azureDevopsId = project.getAzureDevopsId();
            if (StringUtils.isNotBlank(azureDevopsId)) {
                platforms.add(IssuesManagePlatform.AzureDevops.name());
            }
        }

        issueRequest.setWorkspaceId(workspaceId);
        List<AbstractIssuePlatform> platformList = IssueFactory.createPlatforms(platforms, issueRequest);
        platformList.forEach(platform -> {
            List<DemandDTO> demand = platform.getDemandList(projectId);
            list.addAll(demand);
        });

        return list;
    }
}
