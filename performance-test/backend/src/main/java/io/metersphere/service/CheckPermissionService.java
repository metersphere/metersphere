package io.metersphere.service;

import io.metersphere.base.domain.LoadTest;
import io.metersphere.base.domain.LoadTestReport;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional(rollbackFor = Exception.class)
public class CheckPermissionService {

    @Resource
    private BaseCheckPermissionService baseCheckPermissionService;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private LoadTestMapper loadTestMapper;

    public void checkPerformanceTestOwner(String testId, String permissionId) {
        // 关联为其他时
        if (StringUtils.equals("other", testId)) {
            return;
        }
        Set<String> projectIds = baseCheckPermissionService.getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }

        LoadTest loadTest = loadTestMapper.selectByPrimaryKey(testId);
        if (loadTest == null) {
            MSException.throwException(Translator.get("check_owner_test"));
        }
        if (!CollectionUtils.containsAny(projectIds, loadTest.getProjectId())) {
            MSException.throwException(Translator.get("check_owner_test"));
        }

        if (!SessionUtils.hasPermission(null, loadTest.getProjectId(), permissionId)) {
            MSException.throwException(Translator.get("check_owner_case"));
        }
    }

    public void checkPerformanceReportOwner(String reportId, String permissionId) {
        Set<String> projectIds = baseCheckPermissionService.getUserRelatedProjectIds();
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }

        LoadTestReport report = loadTestReportMapper.selectByPrimaryKey(reportId);
        if (report == null) {
            MSException.throwException(Translator.get("check_owner_test"));
        }
        if (!CollectionUtils.containsAny(projectIds, report.getProjectId())) {
            MSException.throwException(Translator.get("check_owner_test"));
        }

        if (!SessionUtils.hasPermission(null, report.getProjectId(), permissionId)) {
            MSException.throwException(Translator.get("check_owner_case"));
        }
    }
}
