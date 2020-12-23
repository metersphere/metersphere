package io.metersphere.service;

import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.UserRole;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.RoleConstants;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CheckPermissionService {
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


    public void checkReadOnlyUser() {
        String currentWorkspaceId = SessionUtils.getCurrentWorkspaceId();
        Set<String> collect = Objects.requireNonNull(SessionUtils.getUser()).getUserRoles().stream()
                .filter(ur ->
                        StringUtils.equals(ur.getRoleId(), RoleConstants.TEST_VIEWER))
                .map(UserRole::getSourceId)
                .filter(sourceId -> StringUtils.equals(currentWorkspaceId, sourceId))
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(collect)) {
            throw new RuntimeException(Translator.get("check_owner_read_only"));
        }
    }

    public void checkProjectOwner(String projectId) {
        Set<String> workspaceIds = getUserRelatedWorkspaceIds();
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            return;
        }
        if (CollectionUtils.isEmpty(workspaceIds)) {
            return;
        }
        if (!workspaceIds.contains(project.getWorkspaceId())) {
            throw new RuntimeException(Translator.get("check_owner_project"));
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
        Set<String> workspaceIds = getUserRelatedWorkspaceIds();
        if (CollectionUtils.isEmpty(workspaceIds)) {
            return;
        }

        int result = extApiTestMapper.checkApiTestOwner(testId, workspaceIds);

        if (result == 0) {
            throw new RuntimeException(Translator.get("check_owner_test"));
        }
    }

    public void checkPerformanceTestOwner(String testId) {
        // 关联为其他时
        if (StringUtils.equals("other", testId)) {
            return;
        }
        Set<String> workspaceIds = getUserRelatedWorkspaceIds();
        if (CollectionUtils.isEmpty(workspaceIds)) {
            return;
        }
        int result = extLoadTestMapper.checkLoadTestOwner(testId, workspaceIds);

        if (result == 0) {
            throw new RuntimeException(Translator.get("check_owner_test"));
        }
    }

    public void checkTestCaseOwner(String caseId) {
        Set<String> workspaceIds = getUserRelatedWorkspaceIds();
        if (CollectionUtils.isEmpty(workspaceIds)) {
            return;
        }

        int result = extTestCaseMapper.checkIsHave(caseId, workspaceIds);
        if (result == 0) {
            throw new RuntimeException(Translator.get("check_owner_case"));
        }
    }

    public void checkTestPlanOwner(String planId) {
        Set<String> workspaceIds = getUserRelatedWorkspaceIds();
        if (CollectionUtils.isEmpty(workspaceIds)) {
            return;
        }
        int result = extTestPlanMapper.checkIsHave(planId, workspaceIds);
        if (result == 0) {
            throw new RuntimeException(Translator.get("check_owner_plan"));
        }
    }

    public void checkTestReviewOwner(String reviewId) {
        Set<String> workspaceIds = getUserRelatedWorkspaceIds();
        if (CollectionUtils.isEmpty(workspaceIds)) {
            return;
        }
        int result = extTestCaseReviewMapper.checkIsHave(reviewId, workspaceIds);
        if (result == 0) {
            throw new RuntimeException(Translator.get("check_owner_review"));
        }
    }
}
