package io.metersphere.service.wapper;

import io.metersphere.base.mapper.ext.ExtTestCaseMapper;
import io.metersphere.base.mapper.ext.ExtTestCaseReviewMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.i18n.Translator;
import io.metersphere.service.BaseCheckPermissionService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Set;

@Service
@Transactional(rollbackFor = Exception.class)
public class CheckPermissionService  {

    @Resource
    private ExtTestCaseMapper extTestCaseMapper;
    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;
    @Resource
    private ExtTestCaseReviewMapper extTestCaseReviewMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;

    public void checkTestCaseOwner(String caseId) {
        Set<String> projectIds = baseCheckPermissionService.getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }

        int result = extTestCaseMapper.checkIsHave(caseId, projectIds);
        if (result == 0) {
            MSException.throwException(Translator.get("check_owner_case"));
        }
    }

    public void checkTestPlanOwner(String planId) {
        Set<String> projectIds = baseCheckPermissionService.getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        int result = extTestPlanMapper.checkIsHave(planId, projectIds);
        if (result == 0) {
            MSException.throwException(Translator.get("check_owner_plan"));
        }
    }

    public void checkTestReviewOwner(String reviewId) {
        Set<String> projectIds = baseCheckPermissionService.getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }
        int result = extTestCaseReviewMapper.checkIsHave(reviewId, projectIds);
        if (result == 0) {
            MSException.throwException(Translator.get("check_owner_review"));
        }
    }
}
