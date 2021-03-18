package io.metersphere.track.service;

import io.metersphere.base.domain.Issues;
import io.metersphere.base.domain.Project;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.service.ProjectService;
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
    private ProjectService projectService;
    @Resource
    private IssuesService issuesService;
    @Resource
    private ProjectMapper projectMapper;

    public List<DemandDTO> getDemandList(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        SessionUser user = SessionUtils.getUser();

        String orgId = user.getLastOrganizationId();
        boolean tapd = issuesService.isIntegratedPlatform(orgId, IssuesManagePlatform.Tapd.toString());
        boolean jira = issuesService.isIntegratedPlatform(orgId, IssuesManagePlatform.Jira.toString());
        boolean zentao = issuesService.isIntegratedPlatform(orgId, IssuesManagePlatform.Zentao.toString());
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

        List<AbstractIssuePlatform> platformList = IssueFactory.createPlatforms(platforms, issueRequest);
        platformList.forEach(platform -> {
            List<DemandDTO> demand = platform.getDemandList(projectId);
            list.addAll(demand);
        });

        return list;
    }
}
