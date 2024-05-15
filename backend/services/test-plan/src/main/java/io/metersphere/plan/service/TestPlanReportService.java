package io.metersphere.plan.service;

import io.metersphere.plan.domain.TestPlanReport;
import io.metersphere.plan.domain.TestPlanReportExample;
import io.metersphere.plan.dto.request.TestPlanReportBatchRequest;
import io.metersphere.plan.dto.request.TestPlanReportPageRequest;
import io.metersphere.plan.dto.response.TestPlanReportPageResponse;
import io.metersphere.plan.mapper.ExtTestPlanReportMapper;
import io.metersphere.plan.mapper.TestPlanReportMapper;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.SubListUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TestPlanReportService {

    @Resource
    private UserService userService;
    @Resource
    private TestPlanReportMapper testPlanReportMapper;
    @Resource
    private ExtTestPlanReportMapper extTestPlanReportMapper;
    @Resource
    private TestPlanReportLogService testPlanReportLogService;
    @Resource
    private TestPlanReportNoticeService testPlanReportNoticeService;
    @Resource
    private UserMapper userMapper;

    /**
     * 分页查询报告列表
     *
     * @param request 分页请求参数
     * @return 报告列表
     */
    public List<TestPlanReportPageResponse> page(TestPlanReportPageRequest request) {
        List<TestPlanReportPageResponse> reportList = extTestPlanReportMapper.list(request);
        if (CollectionUtils.isEmpty(reportList)) {
            return new ArrayList<>();
        }
        List<String> distinctUserIds = reportList.stream().map(TestPlanReportPageResponse::getCreateUser).distinct().toList();
        Map<String, String> userMap = userService.getUserMapByIds(distinctUserIds);
        reportList.forEach(report -> report.setCreateUserName(userMap.get(report.getCreateUser())));
        return reportList;
    }

    /**
     * 报告重命名
     */
    public void rename(String id, String name) {
        TestPlanReport report = checkReport(id);
        report.setName(name);
        testPlanReportMapper.updateByPrimaryKeySelective(report);
    }

    /**
     * 删除单个报告
     */
    public void delete(String id) {
        TestPlanReport report = checkReport(id);
        report.setDeleted(true);
        testPlanReportMapper.updateByPrimaryKeySelective(report);
    }

    /**
     * 批量参数报告
     *
     * @param request 请求参数
     */
    public void batchDelete(TestPlanReportBatchRequest request, String userId) {
        List<String> batchIds = getBatchIds(request);
        User user = userMapper.selectByPrimaryKey(userId);
        if (CollectionUtils.isNotEmpty(batchIds)) {
            SubListUtils.dealForSubList(batchIds, 500, subList -> {
                TestPlanReportExample example = new TestPlanReportExample();
                example.createCriteria().andIdIn(subList);
                TestPlanReport testPlanReport = new TestPlanReport();
                testPlanReport.setDeleted(true);
                testPlanReportMapper.updateByExampleSelective(testPlanReport, example);

                testPlanReportLogService.batchDeleteLog(subList, userId, request.getProjectId());
                testPlanReportNoticeService.batchSendNotice(subList, user, request.getProjectId(), NoticeConstants.Event.DELETE);
            });
        }
    }


    /**
     * 通过请求参数获取批量操作的ID集合
     *
     * @param request 请求参数
     * @return ID集合
     */
    private List<String> getBatchIds(TestPlanReportBatchRequest request) {
        if (request.isSelectAll()) {
            List<String> batchIds = extTestPlanReportMapper.getReportBatchIdsByParam(request);
            if (CollectionUtils.isNotEmpty(request.getExcludeIds())) {
                batchIds.removeIf(id -> request.getExcludeIds().contains(id));
            }
            return batchIds;
        } else {
            return request.getSelectIds();
        }
    }

    /**
     * 校验报告是否存在
     *
     * @param id 报告ID
     */
    private TestPlanReport checkReport(String id) {
        TestPlanReport testPlanReport = testPlanReportMapper.selectByPrimaryKey(id);
        if (testPlanReport == null) {
            throw new MSException(Translator.get("test_plan_report_not_exist"));
        }
        return testPlanReport;
    }
}
