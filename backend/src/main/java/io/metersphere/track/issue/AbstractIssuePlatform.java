package io.metersphere.track.issue;

import io.metersphere.base.domain.ServiceIntegration;
import io.metersphere.base.mapper.IssuesMapper;
import io.metersphere.base.mapper.TestCaseIssuesMapper;
import io.metersphere.base.mapper.ext.ExtIssuesMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.EncryptUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.IntegrationRequest;
import io.metersphere.service.IntegrationService;
import io.metersphere.service.ProjectService;
import io.metersphere.track.request.testcase.IssuesRequest;
import io.metersphere.track.service.TestCaseService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;

public abstract class AbstractIssuePlatform implements IssuesPlatform {

    protected IntegrationService integrationService;
    protected TestCaseIssuesMapper testCaseIssuesMapper;
    protected ProjectService projectService;
    protected TestCaseService testCaseService;
    protected IssuesMapper issuesMapper;
    protected ExtIssuesMapper extIssuesMapper;

    protected String testCaseId;

    public AbstractIssuePlatform(IssuesRequest issuesRequest) {
        this.integrationService = CommonBeanFactory.getBean(IntegrationService.class);
        this.testCaseIssuesMapper = CommonBeanFactory.getBean(TestCaseIssuesMapper.class);
        this.projectService = CommonBeanFactory.getBean(ProjectService.class);
        this.testCaseService = CommonBeanFactory.getBean(TestCaseService.class);
        this.issuesMapper = CommonBeanFactory.getBean(IssuesMapper.class);
        this.extIssuesMapper = CommonBeanFactory.getBean(ExtIssuesMapper.class);
        this.testCaseId = issuesRequest.getTestCaseId();
    }

    protected String getPlatformConfig(String platform) {
        SessionUser user = SessionUtils.getUser();
        String orgId = user.getLastOrganizationId();

        IntegrationRequest request = new IntegrationRequest();
        if (StringUtils.isBlank(orgId)) {
            MSException.throwException("organization id is null");
        }
        request.setOrgId(orgId);
        request.setPlatform(platform);

        ServiceIntegration integration = integrationService.get(request);
        return integration.getConfiguration();
    }

    protected HttpHeaders auth(String apiUser, String password) {
        String authKey = EncryptUtils.base64Encoding(apiUser + ":" + password);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + authKey);
        return headers;
    }

    /**
     * 获取平台与项目相关的属性
     * @return 其他平台和本地项目绑定的属性值
     */
    abstract String getProjectId();

    protected boolean isIntegratedPlatform(String orgId, String platform) {
        IntegrationRequest request = new IntegrationRequest();
        request.setPlatform(platform);
        request.setOrgId(orgId);
        ServiceIntegration integration = integrationService.get(request);
        return StringUtils.isNotBlank(integration.getId());
    }

}
