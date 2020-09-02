package io.metersphere.performance.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportDetailMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.config.KafkaProperties;
import io.metersphere.controller.request.OrderRequest;
import io.metersphere.controller.request.QueryScheduleRequest;
import io.metersphere.dto.DashboardTestDTO;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.dto.ScheduleDao;
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.PerformanceTestJob;
import io.metersphere.performance.engine.Engine;
import io.metersphere.performance.engine.EngineFactory;
import io.metersphere.service.FileService;
import io.metersphere.service.QuotaService;
import io.metersphere.service.ScheduleService;
import io.metersphere.service.TestResourceService;
import io.metersphere.track.request.testplan.*;
import io.metersphere.track.service.TestCaseService;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class PerformanceTestService {
    public static final String HEADERS = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect";

    @Resource
    private LoadTestMapper loadTestMapper;
    @Resource
    private ExtLoadTestMapper extLoadTestMapper;
    @Resource
    private LoadTestFileMapper loadTestFileMapper;
    @Resource
    private FileService fileService;
    @Resource
    private LoadTestReportMapper loadTestReportMapper;
    @Resource
    private ExtLoadTestReportMapper extLoadTestReportMapper;
    @Resource
    private LoadTestReportDetailMapper loadTestReportDetailMapper;
    @Resource
    private ExtLoadTestReportDetailMapper extLoadTestReportDetailMapper;
    @Resource
    private LoadTestReportLogMapper loadTestReportLogMapper;
    @Resource
    private LoadTestReportResultMapper loadTestReportResultMapper;
    @Resource
    private TestResourceService testResourceService;
    @Resource
    private ReportService reportService;
    @Resource
    private KafkaProperties kafkaProperties;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private TestCaseMapper testCaseMapper;
    @Resource
    private TestCaseService testCaseService;

    public List<LoadTestDTO> list(QueryTestPlanRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extLoadTestMapper.list(request);
    }

    public void delete(DeleteTestPlanRequest request) {
        String testId = request.getId();

        testCaseService.checkIsRelateTest(testId);

        LoadTestReportExample loadTestReportExample = new LoadTestReportExample();
        loadTestReportExample.createCriteria().andTestIdEqualTo(testId);
        List<LoadTestReport> loadTestReports = loadTestReportMapper.selectByExample(loadTestReportExample);

        if (!loadTestReports.isEmpty()) {
            List<String> reportIdList = loadTestReports.stream().map(LoadTestReport::getId).collect(Collectors.toList());

            // delete load_test_report
            reportIdList.forEach(reportId -> {
                reportService.deleteReport(reportId);
            });
        }

        //delete schedule
        scheduleService.deleteByResourceId(testId);

        // delete load_test
        loadTestMapper.deleteByPrimaryKey(request.getId());

        deleteFileByTestId(request.getId());
    }

    public void deleteFileByTestId(String testId) {
        LoadTestFileExample loadTestFileExample = new LoadTestFileExample();
        loadTestFileExample.createCriteria().andTestIdEqualTo(testId);
        final List<LoadTestFile> loadTestFiles = loadTestFileMapper.selectByExample(loadTestFileExample);
        loadTestFileMapper.deleteByExample(loadTestFileExample);

        if (!CollectionUtils.isEmpty(loadTestFiles)) {
            final List<String> fileIds = loadTestFiles.stream().map(LoadTestFile::getFileId).collect(Collectors.toList());
            fileService.deleteFileByIds(fileIds);
        }
    }

    public String save(SaveTestPlanRequest request, List<MultipartFile> files) {
        if (files == null) {
            throw new IllegalArgumentException(Translator.get("file_cannot_be_null"));
        }

        checkQuota(request, true);

        final LoadTestWithBLOBs loadTest = saveLoadTest(request);
        files.forEach(file -> {
            final FileMetadata fileMetadata = fileService.saveFile(file);
            LoadTestFile loadTestFile = new LoadTestFile();
            loadTestFile.setTestId(loadTest.getId());
            loadTestFile.setFileId(fileMetadata.getId());
            loadTestFileMapper.insert(loadTestFile);
        });
        return loadTest.getId();
    }

    private LoadTestWithBLOBs saveLoadTest(SaveTestPlanRequest request) {

        LoadTestExample example = new LoadTestExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId());
        if (loadTestMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("load_test_already_exists"));
        }

        final LoadTestWithBLOBs loadTest = new LoadTestWithBLOBs();
        loadTest.setUserId(SessionUtils.getUser().getId());
        loadTest.setId(UUID.randomUUID().toString());
        loadTest.setName(request.getName());
        loadTest.setProjectId(request.getProjectId());
        loadTest.setCreateTime(System.currentTimeMillis());
        loadTest.setUpdateTime(System.currentTimeMillis());
        loadTest.setTestResourcePoolId(request.getTestResourcePoolId());
        loadTest.setLoadConfiguration(request.getLoadConfiguration());
        loadTest.setAdvancedConfiguration(request.getAdvancedConfiguration());
        loadTest.setStatus(PerformanceTestStatus.Saved.name());
        loadTestMapper.insert(loadTest);
        return loadTest;
    }

    public String edit(EditTestPlanRequest request, List<MultipartFile> files) {
        checkQuota(request, false);
        //
        LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(request.getId());
        if (loadTest == null) {
            MSException.throwException(Translator.get("edit_load_test_not_found") + request.getId());
        }
        if (StringUtils.containsAny(loadTest.getStatus(), PerformanceTestStatus.Running.name(), PerformanceTestStatus.Starting.name())) {
            MSException.throwException(Translator.get("cannot_edit_load_test_running"));
        }
        // 新选择了一个文件，删除原来的文件
        List<FileMetadata> updatedFiles = request.getUpdatedFileList();
        List<FileMetadata> originFiles = fileService.getFileMetadataByTestId(request.getId());
        List<String> updatedFileIds = updatedFiles.stream().map(FileMetadata::getId).collect(Collectors.toList());
        List<String> originFileIds = originFiles.stream().map(FileMetadata::getId).collect(Collectors.toList());
        // 相减
        List<String> deleteFileIds = ListUtils.subtract(originFileIds, updatedFileIds);
        fileService.deleteFileByIds(deleteFileIds);

        if (files != null) {
            files.forEach(file -> {
                final FileMetadata fileMetadata = fileService.saveFile(file);
                LoadTestFile loadTestFile = new LoadTestFile();
                loadTestFile.setTestId(request.getId());
                loadTestFile.setFileId(fileMetadata.getId());
                loadTestFileMapper.insert(loadTestFile);
            });
        }

        loadTest.setName(request.getName());
        loadTest.setProjectId(request.getProjectId());
        loadTest.setUpdateTime(System.currentTimeMillis());
        loadTest.setLoadConfiguration(request.getLoadConfiguration());
        loadTest.setAdvancedConfiguration(request.getAdvancedConfiguration());
        loadTest.setTestResourcePoolId(request.getTestResourcePoolId());
        loadTest.setStatus(PerformanceTestStatus.Saved.name());
        loadTestMapper.updateByPrimaryKeySelective(loadTest);

        return request.getId();
    }

    @Transactional(noRollbackFor = MSException.class)//  保存失败的信息
    public String run(RunTestPlanRequest request) {
        final LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(request.getId());
        if (request.getUserId() != null) {
            loadTest.setUserId(request.getUserId());
        }
        if (loadTest == null) {
            MSException.throwException(Translator.get("run_load_test_not_found") + request.getId());
        }

        if (StringUtils.equalsAny(loadTest.getStatus(), PerformanceTestStatus.Running.name(), PerformanceTestStatus.Starting.name())) {
            MSException.throwException(Translator.get("load_test_is_running"));
        }
        // check kafka
        checkKafka();

        LogUtil.info("Load test started " + loadTest.getName());
        // engine type (NODE)
        final Engine engine = EngineFactory.createEngine(loadTest);
        if (engine == null) {
            MSException.throwException(String.format("Test cannot be run，test ID：%s", request.getId()));
        }

        startEngine(loadTest, engine, request.getTriggerMode());

        return engine.getReportId();
    }

    private void checkKafka() {
        String bootstrapServers = kafkaProperties.getBootstrapServers();
        String[] servers = StringUtils.split(bootstrapServers, ",");
        try {
            for (String s : servers) {
                String[] ipAndPort = s.split(":");
                //1,建立tcp
                String ip = ipAndPort[0];
                int port = Integer.parseInt(ipAndPort[1]);
                Socket soc = new Socket();
                soc.connect(new InetSocketAddress(ip, port), 1000); // 1s timeout
                //2.输入内容
                String content = "1010";
                byte[] bs = content.getBytes();
                OutputStream os = soc.getOutputStream();
                os.write(bs);
                //3.关闭
                soc.close();
            }
        } catch (Exception e) {
            LogUtil.error(e);
            MSException.throwException(Translator.get("load_test_kafka_invalid"));
        }
    }

    private void startEngine(LoadTestWithBLOBs loadTest, Engine engine, String triggerMode) {
        LoadTestReportWithBLOBs testReport = new LoadTestReportWithBLOBs();
        testReport.setId(engine.getReportId());
        testReport.setCreateTime(engine.getStartTime());
        testReport.setUpdateTime(engine.getStartTime());
        testReport.setTestId(loadTest.getId());
        testReport.setName(loadTest.getName());
        testReport.setTriggerMode(triggerMode);
        if (SessionUtils.getUser() == null) {
            testReport.setUserId(loadTest.getUserId());
        } else {
            testReport.setUserId(SessionUtils.getUser().getId());
        }
        // 启动测试

        try {
            engine.start();
            // 启动正常修改状态 starting
            loadTest.setStatus(PerformanceTestStatus.Starting.name());
            loadTestMapper.updateByPrimaryKeySelective(loadTest);
            // 启动正常插入 report
            testReport.setLoadConfiguration(loadTest.getLoadConfiguration());
            testReport.setStatus(PerformanceTestStatus.Starting.name());
            loadTestReportMapper.insertSelective(testReport);

            LoadTestReportDetail reportDetail = new LoadTestReportDetail();
            reportDetail.setContent(HEADERS);
            reportDetail.setReportId(testReport.getId());
            reportDetail.setPart(1L);
            loadTestReportDetailMapper.insertSelective(reportDetail);
            // append \n
            extLoadTestReportDetailMapper.appendLine(testReport.getId(), "\n");
            // 保存一个 reportStatus
            LoadTestReportResult reportResult = new LoadTestReportResult();
            reportResult.setId(UUID.randomUUID().toString());
            reportResult.setReportId(testReport.getId());
            reportResult.setReportKey(ReportKeys.ResultStatus.name());
            reportResult.setReportValue("Ready"); // 初始化一个 result_status, 这个值用在data-streaming中
            loadTestReportResultMapper.insertSelective(reportResult);
        } catch (MSException e) {
            LogUtil.error(e);
            loadTest.setStatus(PerformanceTestStatus.Error.name());
            loadTest.setDescription(e.getMessage());
            loadTestMapper.updateByPrimaryKeySelective(loadTest);
            throw e;
        }
    }

    public List<LoadTestDTO> recentTestPlans(QueryTestPlanRequest request) {
        // 查询最近的测试计划
        List<OrderRequest> orders = new ArrayList<>();
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setName("update_time");
        orderRequest.setType("desc");
        orders.add(orderRequest);
        request.setOrders(orders);
        return extLoadTestMapper.list(request);
    }

    public LoadTestDTO get(String testId) {
        QueryTestPlanRequest request = new QueryTestPlanRequest();
        request.setId(testId);
        List<LoadTestDTO> testDTOS = extLoadTestMapper.list(request);
        if (!CollectionUtils.isEmpty(testDTOS)) {
            LoadTestDTO loadTestDTO = testDTOS.get(0);
            Schedule schedule = scheduleService.getScheduleByResource(loadTestDTO.getId(), ScheduleGroup.PERFORMANCE_TEST.name());
            loadTestDTO.setSchedule(schedule);
            return loadTestDTO;
        }
        return null;
    }

    public String getAdvancedConfiguration(String testId) {
        LoadTestWithBLOBs loadTestWithBLOBs = loadTestMapper.selectByPrimaryKey(testId);
        return Optional.ofNullable(loadTestWithBLOBs).orElse(new LoadTestWithBLOBs()).getAdvancedConfiguration();
    }

    public String getLoadConfiguration(String testId) {
        LoadTestWithBLOBs loadTestWithBLOBs = loadTestMapper.selectByPrimaryKey(testId);
        return Optional.ofNullable(loadTestWithBLOBs).orElse(new LoadTestWithBLOBs()).getLoadConfiguration();
    }

    public List<LoadTestWithBLOBs> selectByTestResourcePoolId(String resourcePoolId) {
        LoadTestExample example = new LoadTestExample();
        example.createCriteria().andTestResourcePoolIdEqualTo(resourcePoolId);
        return loadTestMapper.selectByExampleWithBLOBs(example);
    }

    public List<DashboardTestDTO> dashboardTests(String workspaceId) {
        Instant oneYearAgo = Instant.now().plus(-365, ChronoUnit.DAYS);
        long startTimestamp = oneYearAgo.toEpochMilli();
        return extLoadTestReportMapper.selectDashboardTests(workspaceId, startTimestamp);
    }

    public List<LoadTest> getLoadTestByProjectId(String projectId) {
        return extLoadTestMapper.getLoadTestByProjectId(projectId);
    }

    public LoadTest getLoadTestBytestId(String testId) {
        return loadTestMapper.selectByPrimaryKey(testId);
    }

    public void copy(SaveTestPlanRequest request) {
        checkQuota(request, true);
        // copy test
        LoadTestWithBLOBs copy = loadTestMapper.selectByPrimaryKey(request.getId());
        copy.setId(UUID.randomUUID().toString());
        copy.setName(copy.getName() + " Copy");
        copy.setCreateTime(System.currentTimeMillis());
        copy.setUpdateTime(System.currentTimeMillis());
        copy.setStatus(APITestStatus.Saved.name());
        copy.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        loadTestMapper.insert(copy);
        // copy test file
        LoadTestFileExample loadTestFileExample = new LoadTestFileExample();
        loadTestFileExample.createCriteria().andTestIdEqualTo(request.getId());
        List<LoadTestFile> loadTestFiles = loadTestFileMapper.selectByExample(loadTestFileExample);
        if (!CollectionUtils.isEmpty(loadTestFiles)) {
            loadTestFiles.forEach(loadTestFile -> {
                FileMetadata fileMetadata = fileService.copyFile(loadTestFile.getFileId());
                if (fileMetadata == null) {
                    // 如果性能测试出现文件变更，这里会有 null
                    return;
                }
                loadTestFile.setTestId(copy.getId());
                loadTestFile.setFileId(fileMetadata.getId());
                loadTestFileMapper.insert(loadTestFile);
            });
        }
    }

    public void updateSchedule(Schedule request) {
        scheduleService.editSchedule(request);
        addOrUpdatePerformanceTestCronJob(request);
    }

    public void createSchedule(Schedule request) {
        scheduleService.addSchedule(buildPerformanceTestSchedule(request));
        addOrUpdatePerformanceTestCronJob(request);
    }

    private Schedule buildPerformanceTestSchedule(Schedule request) {
        Schedule schedule = scheduleService.buildApiTestSchedule(request);
        schedule.setJob(PerformanceTestJob.class.getName());
        schedule.setGroup(ScheduleGroup.PERFORMANCE_TEST.name());
        schedule.setType(ScheduleType.CRON.name());
        return schedule;
    }

    private void addOrUpdatePerformanceTestCronJob(Schedule request) {
        scheduleService.addOrUpdateCronJob(request, PerformanceTestJob.getJobKey(request.getResourceId()), PerformanceTestJob.getTriggerKey(request.getResourceId()), PerformanceTestJob.class);
    }

    public void stopTest(String reportId, boolean forceStop) {
        if (forceStop) {
            reportService.deleteReport(reportId);
        } else {
            LoadTestReport loadTestReport = loadTestReportMapper.selectByPrimaryKey(reportId);
            LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(loadTestReport.getTestId());
            final Engine engine = EngineFactory.createEngine(loadTest);
            if (engine == null) {
                MSException.throwException(String.format("Stop report fail. create engine fail，report ID：%s", reportId));
            }
            reportService.stopEngine(loadTest, engine);
            // 停止测试之后设置报告的状态
            reportService.updateStatus(reportId, PerformanceTestStatus.Completed.name());
        }
    }

    public List<ScheduleDao> listSchedule(QueryScheduleRequest request) {
        request.setEnable(true);
        List<ScheduleDao> schedules = scheduleService.list(request);
        List<String> resourceIds = schedules.stream()
                .map(Schedule::getResourceId)
                .collect(Collectors.toList());
        if (!resourceIds.isEmpty()) {
            LoadTestExample example = new LoadTestExample();
            example.createCriteria().andIdIn(resourceIds);
            List<LoadTest> loadTests = loadTestMapper.selectByExample(example);
            Map<String, String> loadTestMap = loadTests.stream().collect(Collectors.toMap(LoadTest::getId, LoadTest::getName));
            scheduleService.build(loadTestMap, schedules);
        }
        return schedules;
    }

    private void checkQuota(TestPlanRequest request, boolean create) {
        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        if (quotaService != null) {
            quotaService.checkLoadTestQuota(request, create);
        }
    }
}
