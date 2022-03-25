package io.metersphere.service;

import io.metersphere.base.domain.Group;
import io.metersphere.base.domain.UserGroup;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.UserGroupType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.dto.ProjectDTO;
import io.metersphere.dto.UserDTO;
import io.metersphere.i18n.Translator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class CheckPermissionService {
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
    @Resource
    private UserService userService;
    @Resource
    private ExtProjectMapper extProjectMapper;


    public void checkProjectOwner(String projectId) {
        Set<String> projectIds = getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        if (!projectIds.contains(projectId)) {
            MSException.throwException(Translator.get("check_owner_project"));
        }
    }

    public Set<String> getUserRelatedProjectIds() {
        UserDTO userDTO = userService.getUserDTO(SessionUtils.getUserId());
        List<String> groupIds = userDTO.getGroups()
                .stream()
                .filter(g -> StringUtils.equals(g.getType(), UserGroupType.PROJECT))
                .map(Group::getId)
                .collect(Collectors.toList());
        return userDTO.getUserGroups().stream()
                .filter(ur -> groupIds.contains(ur.getGroupId()))
                .map(UserGroup::getSourceId)
                .collect(Collectors.toSet());
    }

    public void checkApiTestOwner(String testId) {
        // 关联为其他时
        if (StringUtils.equals("other", testId)) {
            return;
        }
        Set<String> projectIds = getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }

        int result = extApiTestMapper.checkApiTestOwner(testId, projectIds);

        if (result == 0) {
            MSException.throwException(Translator.get("check_owner_test"));
        }
    }

    public void checkPerformanceTestOwner(String testId) {
        // 关联为其他时
        if (StringUtils.equals("other", testId)) {
            return;
        }
        Set<String> projectIds = getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        int result = extLoadTestMapper.checkLoadTestOwner(testId, projectIds);

        if (result == 0) {
            MSException.throwException(Translator.get("check_owner_test"));
        }
    }

    public void checkTestCaseOwner(String caseId) {
        Set<String> projectIds = getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }

        int result = extTestCaseMapper.checkIsHave(caseId, projectIds);
        if (result == 0) {
            MSException.throwException(Translator.get("check_owner_case"));
        }
    }

    public void checkTestPlanOwner(String planId) {
        Set<String> projectIds = getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        int result = extTestPlanMapper.checkIsHave(planId, projectIds);
        if (result == 0) {
            MSException.throwException(Translator.get("check_owner_plan"));
        }
    }

    public void checkTestReviewOwner(String reviewId) {
        Set<String> projectIds = getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        int result = extTestCaseReviewMapper.checkIsHave(reviewId, projectIds);
        if (result == 0) {
            MSException.throwException(Translator.get("check_owner_review"));
        }
    }

    public List<ProjectDTO> getOwnerProjects() {
        Set<String> userRelatedProjectIds = getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(userRelatedProjectIds)) {
            return new ArrayList<>(0);
        }
        List<String> projectIds = new ArrayList<>(userRelatedProjectIds);
        return extProjectMapper.queryListByIds(projectIds);
    }

    public Set<String> getOwnerByUserId(String userId) {
        UserDTO userDTO = userService.getUserDTO(userId);
        List<String> groupIds = userDTO.getGroups()
                .stream()
                .filter(g -> StringUtils.equals(g.getType(), UserGroupType.PROJECT))
                .map(Group::getId)
                .collect(Collectors.toList());
        return userDTO.getUserGroups().stream()
                .filter(ur -> groupIds.contains(ur.getGroupId()))
                .map(UserGroup::getSourceId)
                .collect(Collectors.toSet());
    }
}
