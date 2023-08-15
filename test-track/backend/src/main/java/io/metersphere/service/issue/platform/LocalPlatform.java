package io.metersphere.service.issue.platform;


import io.metersphere.base.domain.IssuesWithBLOBs;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.CustomFieldItemDTO;
import io.metersphere.request.testcase.TestCaseBatchRequest;
import io.metersphere.xpack.track.dto.AttachmentSyncType;
import io.metersphere.xpack.track.dto.DemandDTO;
import io.metersphere.xpack.track.dto.request.IssuesRequest;
import io.metersphere.xpack.track.dto.request.IssuesUpdateRequest;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class LocalPlatform extends LocalAbstractPlatform {

    protected String key = IssuesManagePlatform.Local.toString();

    public LocalPlatform(IssuesRequest issuesRequest) {
        super(issuesRequest);
    }

    @Override
    public List<DemandDTO> getDemandList(String projectId) {
        return null;
    }

    @Override
    public IssuesWithBLOBs addIssue(IssuesUpdateRequest issuesRequest) {
        String issueStatus = "new";
        if (StringUtils.isNotBlank(issuesRequest.getCustomFields())) {
            List<CustomFieldItemDTO> customFields = issuesRequest.getRequestFields();
            String customFieldStr = JSON.toJSONString(customFields);
            List<TestCaseBatchRequest.CustomFiledRequest> fields = JSON.parseArray(customFieldStr, TestCaseBatchRequest.CustomFiledRequest.class);
            for (TestCaseBatchRequest.CustomFiledRequest field : fields)
                if (StringUtils.equals("状态", field.getName())) {
                    issueStatus = (String) field.getValue();
                    break;
                }
        }
        SessionUser user = SessionUtils.getUser();
        String id = UUID.randomUUID().toString();
        IssuesWithBLOBs issues = new IssuesWithBLOBs();
        BeanUtils.copyBean(issues, issuesRequest);
        issues.setId(id);
        issues.setPlatformId(id);
        issues.setStatus(issueStatus);
        issues.setReporter(user.getId());
        issues.setCreateTime(System.currentTimeMillis());
        issues.setUpdateTime(System.currentTimeMillis());
        issues.setPlatform(IssuesManagePlatform.Local.toString());
        issues.setNum(getNextNum(issuesRequest.getProjectId()));
        issues.setCreator(SessionUtils.getUserId());
        issuesMapper.insert(issues);

        issuesRequest.setId(id);
        handleTestCaseIssues(issuesRequest);

        return issues;
    }

    @Override
    public void updateIssue(IssuesUpdateRequest request) {
        handleIssueUpdate(request);
    }

    @Override
    public void syncIssuesAttachment(IssuesUpdateRequest issuesRequest, File file, AttachmentSyncType syncType) {
        // 不需要同步
    }

}
