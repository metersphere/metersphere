package io.metersphere.listener;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.LoadTestMapper;
import io.metersphere.base.mapper.LoadTestReportMapper;
import io.metersphere.base.mapper.ProjectApplicationMapper;
import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.request.DeleteTestPlanRequest;
import io.metersphere.service.PerformanceReportService;
import io.metersphere.service.PerformanceTestService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectDeletedListener {
    public static final String CONSUME_ID = "project-deleted";
    @Resource
    private PerformanceTestService performanceTestService;
    @Resource
    private PerformanceReportService performanceReportService;
    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.PROJECT_DELETED_TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        String projectId = record.value();
        LogUtil.info("performance service consume project_delete message, project id: " + projectId);
        deleteLoadTestResourcesByProjectId(projectId);
    }


    private void deleteLoadTestResourcesByProjectId(String projectId) {
        LoadTestExample loadTestExample = new LoadTestExample();
        loadTestExample.createCriteria().andProjectIdEqualTo(projectId);
        List<LoadTest> loadTests = loadTestMapper.selectByExample(loadTestExample);
        List<String> loadTestIdList = loadTests.stream().map(LoadTest::getId).collect(Collectors.toList());
        loadTestIdList.forEach(loadTestId -> {
            DeleteTestPlanRequest deleteTestPlanRequest = new DeleteTestPlanRequest();
            deleteTestPlanRequest.setId(loadTestId);
            deleteTestPlanRequest.setForceDelete(true);
            performanceTestService.delete(deleteTestPlanRequest);
            LoadTestReportExample loadTestReportExample = new LoadTestReportExample();
            loadTestReportExample.createCriteria().andTestIdEqualTo(loadTestId);
            List<LoadTestReport> loadTestReports = loadTestReportMapper.selectByExample(loadTestReportExample);
            if (!loadTestReports.isEmpty()) {
                List<String> reportIdList = loadTestReports.stream().map(LoadTestReport::getId).collect(Collectors.toList());
                // delete load_test_report
                reportIdList.forEach(reportId -> performanceReportService.deleteReport(reportId));
            }
        });
        //删除分享报告时间
        delReportTime(projectId, "PERFORMANCE");
    }

    private void delReportTime(String projectId, String type) {
        ProjectApplicationExample projectApplicationExample = new ProjectApplicationExample();
        projectApplicationExample.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(type);
        projectApplicationMapper.deleteByExample(projectApplicationExample);
    }
}
