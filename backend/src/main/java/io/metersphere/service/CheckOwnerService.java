package io.metersphere.service;

import io.metersphere.api.dto.APITestResult;
import io.metersphere.api.dto.QueryAPITestRequest;
import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.UserRole;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.track.dto.TestPlanDTOWithMetric;
import io.metersphere.track.request.testplan.QueryTestPlanRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CheckOwnerService {
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ExtApiTestMapper extApiTestMapper;
    @Resource
    private ExtLoadTestMapper extLoadTestMapper;
    @Resource
    private ExtTestCaseMapper extTestCaseMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private ExtTestCaseReviewMapper extTestCaseReviewMapper;

    public void checkProjectOwner(String projectId) {
        Set<String> workspaceIds = getUserRelatedWorkspaceIds();
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return;
        }
        if (!workspaceIds.contains(project.getWorkspaceId())) {
            throw new UnauthorizedException(Translator.get("check_owner_project"));
        }
    }

    private Set<String> getUserRelatedWorkspaceIds() {
        return Objects.requireNonNull(SessionUtils.getUser()).getUserRoles().stream()
                .filter(ur ->
                        StringUtils.equalsAny(ur.getRoleId(), RoleConstants.TEST_MANAGER, RoleConstants.TEST_USER, RoleConstants.TEST_VIEWER))
                .map(UserRole::getSourceId)
                .collect(Collectors.toSet());
    }

    public void checkApiTestOwner(String testId) {
        // 关联为其他时
        if (StringUtils.equals("other", testId)) {
            return;
        }
        String workspaceId = SessionUtils.getCurrentWorkspaceId();
        QueryAPITestRequest request = new QueryAPITestRequest();
        request.setWorkspaceId(workspaceId);
        request.setId(testId);
        List<APITestResult> apiTestResults = extApiTestMapper.list(request);

        if (CollectionUtils.size(apiTestResults) != 1) {
            throw new UnauthorizedException(Translator.get("check_owner_test"));
        }
    }

    public void checkPerformanceTestOwner(String testId) {
        // 关联为其他时
        if (StringUtils.equals("other", testId)) {
            return;
        }
        String workspaceId = SessionUtils.getCurrentWorkspaceId();
        QueryTestPlanRequest request = new QueryTestPlanRequest();
        request.setWorkspaceId(workspaceId);
        request.setId(testId);
        List<LoadTestDTO> loadTestDTOS = extLoadTestMapper.list(request);

        if (CollectionUtils.size(loadTestDTOS) != 1) {
            throw new UnauthorizedException(Translator.get("check_owner_test"));
        }
    }

    public void checkTestCaseOwner(String caseId) {
        String workspaceId = SessionUtils.getCurrentWorkspaceId();
        List<String> list = extTestCaseMapper.checkIsHave(caseId, workspaceId);
        if (CollectionUtils.size(list) != 1) {
            throw new UnauthorizedException(Translator.get("check_owner_case"));
        }
    }

    public void checkTestPlanOwner(String planId) {
        String workspaceId = SessionUtils.getCurrentWorkspaceId();
        io.metersphere.track.request.testcase.QueryTestPlanRequest request = new io.metersphere.track.request.testcase.QueryTestPlanRequest();
        request.setWorkspaceId(workspaceId);
        request.setId(planId);
        List<TestPlanDTOWithMetric> list = extTestPlanMapper.list(request);
        if (CollectionUtils.size(list) != 1) {
            throw new UnauthorizedException(Translator.get("check_owner_plan"));
        }
    }

    public void checkTestReviewOwner(String reviewId) {
        String workspaceId = SessionUtils.getCurrentWorkspaceId();
        List<String> list = extTestCaseReviewMapper.checkIsHave(reviewId, workspaceId);
        if (CollectionUtils.size(list) != 1) {
            throw new UnauthorizedException(Translator.get("check_owner_review"));
        }
    }
}
