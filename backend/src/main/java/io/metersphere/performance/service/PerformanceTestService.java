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
import io.metersphere.controller.request.ScheduleRequest;
import io.metersphere.dto.DashboardTestDTO;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.dto.ScheduleDao;
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.PerformanceTestJob;
import io.metersphere.performance.dto.LoadTestExportJmx;
import io.metersphere.performance.engine.Engine;
import io.metersphere.performance.engine.EngineFactory;
import io.metersphere.performance.engine.producer.LoadTestProducer;
import io.metersphere.performance.request.*;
import io.metersphere.service.FileService;
import io.metersphere.service.QuotaService;
import io.metersphere.service.ScheduleService;
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
import java.nio.charset.StandardCharsets;
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
    private LoadTestReportResultMapper loadTestReportResultMapper;
    @Resource
    private ReportService reportService;
    @Resource
    private KafkaProperties kafkaProperties;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private TestCaseService testCaseService;
    @Resource
    private TestResourcePoolMapper testResourcePoolMapper;
    @Resource
    private LoadTestProducer loadTestProducer;

    public List<LoadTestDTO> list(QueryTestPlanRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extLoadTestMapper.list(request);
    }

    public void delete(DeleteTestPlanRequest request) {
        String testId = request.getId();

        if (!request.isForceDelete()) {
            testCaseService.checkIsRelateTest(testId);
        }

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
            List<String> fileIds = loadTestFiles.stream().map(LoadTestFile::getFileId).collect(Collectors.toList());
            LoadTestFileExample example3 = new LoadTestFileExample();
            example3.createCriteria().andFileIdIn(fileIds);
            loadTestFileMapper.deleteByExample(example3);
        }
    }

    public String save(SaveTestPlanRequest request, List<MultipartFile> files) {
        checkQuota(request, true);
        LoadTestWithBLOBs loadTest = saveLoadTest(request);

        List<FileMetadata> importFiles = request.getUpdatedFileList();
        List<String> importFileIds = importFiles.stream().map(FileMetadata::getId).collect(Collectors.toList());
        // 导入项目里其他的文件
        this.importFiles(importFileIds, loadTest.getId(), request.getFileSorts());
        // 保存上传的文件
        this.saveUploadFiles(files, loadTest, request.getFileSorts());
        //关联转化的文件
        this.conversionFiles(loadTest.getId(), request.getConversionFileIdList());
        return loadTest.getId();
    }

    private void conversionFiles(String id, List<String> conversionFileIdList) {
        for (String metaFileId : conversionFileIdList) {
            if (!this.loadTestFileExsits(id, metaFileId)) {
                LoadTestFile loadTestFile = new LoadTestFile();
                loadTestFile.setTestId(id);
                loadTestFile.setFileId(metaFileId);
                loadTestFileMapper.insert(loadTestFile);
            }
        }
    }

    private boolean loadTestFileExsits(String testId, String metaFileId) {
        boolean fileExsits = fileService.isFileExsits(metaFileId);
        LoadTestFileExample example = new LoadTestFileExample();
        example.createCriteria().andTestIdEqualTo(testId).andFileIdEqualTo(metaFileId);
        long loadTestFiles = loadTestFileMapper.countByExample(example);

        if (!fileExsits && loadTestFiles > 0) {
            return false;
        } else {
            return true;
        }
    }

    private void saveUploadFiles(List<MultipartFile> files, LoadTest loadTest, Map<String, Integer> fileSorts) {
        if (files != null) {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                FileMetadata fileMetadata = fileService.saveFile(file, loadTest.getProjectId());
                LoadTestFile loadTestFile = new LoadTestFile();
                loadTestFile.setTestId(loadTest.getId());
                loadTestFile.setFileId(fileMetadata.getId());
                loadTestFile.setSort(fileSorts.getOrDefault(file.getOriginalFilename(), i));
                loadTestFileMapper.insert(loadTestFile);
            }
        }
    }

    private void importFiles(List<String> importFileIds, String testId, Map<String, Integer> fileSorts) {
        for (int i = 0; i < importFileIds.size(); i++) {
            String fileId = importFileIds.get(i);
            FileMetadata fileMetadata = fileService.getFileMetadataById(fileId);
            LoadTestFile loadTestFile = new LoadTestFile();
            loadTestFile.setTestId(testId);
            loadTestFile.setFileId(fileId);
            loadTestFile.setSort(fileSorts.getOrDefault(fileMetadata.getName(), i));
            loadTestFileMapper.insert(loadTestFile);
        }
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
        loadTest.setNum(getNextNum(request.getProjectId()));
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
        List<FileMetadata> originFiles = getFileMetadataByTestId(request.getId());
        List<String> updatedFileIds = updatedFiles.stream().map(FileMetadata::getId).collect(Collectors.toList());
        List<String> originFileIds = originFiles.stream().map(FileMetadata::getId).collect(Collectors.toList());

        // 相减
        List<String> deleteFileIds = ListUtils.subtract(originFileIds, updatedFileIds);
        // 删除已经不相关的文件
        if (!CollectionUtils.isEmpty(deleteFileIds)) {
            LoadTestFileExample example3 = new LoadTestFileExample();
            example3.createCriteria().andFileIdIn(deleteFileIds);
            loadTestFileMapper.deleteByExample(example3);
        }

        // 导入项目里其他的文件
        List<String> addFileIds = ListUtils.subtract(updatedFileIds, originFileIds);
        this.importFiles(addFileIds, request.getId(), request.getFileSorts());

        // 处理新上传的文件
        this.saveUploadFiles(files, loadTest, request.getFileSorts());

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
        LogUtil.info("性能测试run测试");
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
        String testResourcePoolId = loadTest.getTestResourcePoolId();
        TestResourcePool testResourcePool = testResourcePoolMapper.selectByPrimaryKey(testResourcePoolId);
        if (testResourcePool == null) {
            MSException.throwException("Test resource pool not exists.");
        }
        if (ResourceStatusEnum.INVALID.name().equals(testResourcePool.getStatus())) {
            MSException.throwException("Test resource pool invalid.");
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
            LogUtil.error(e.getMessage(), e);
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
            // 启动失败之后清理任务
            engine.stop();
            LogUtil.error(e.getMessage(), e);
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
        request.setProjectId(SessionUtils.getCurrentProjectId());
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

    public List<LoadTestExportJmx> getJmxContent(String testId) {
        List<FileMetadata> fileMetadataList = getFileMetadataByTestId(testId);
        List<LoadTestExportJmx> results = new ArrayList<>();
        for (FileMetadata metadata : fileMetadataList) {
            if (FileType.JMX.name().equals(metadata.getType())) {
                FileContent fileContent = fileService.getFileContent(metadata.getId());
                results.add(new LoadTestExportJmx(metadata.getName(), new String(fileContent.getFile(), StandardCharsets.UTF_8)));
            }
        }
        return results;
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
        copy.setNum(getNextNum(copy.getProjectId()));
        loadTestMapper.insert(copy);
        // copy test file
        LoadTestFileExample loadTestFileExample = new LoadTestFileExample();
        loadTestFileExample.createCriteria().andTestIdEqualTo(request.getId());
        List<LoadTestFile> loadTestFiles = loadTestFileMapper.selectByExample(loadTestFileExample);
        if (!CollectionUtils.isEmpty(loadTestFiles)) {
            loadTestFiles.forEach(loadTestFile -> {
                loadTestFile.setTestId(copy.getId());
                loadTestFileMapper.insert(loadTestFile);
            });
        }
    }

    public void updateSchedule(Schedule request) {
        scheduleService.editSchedule(request);
        addOrUpdatePerformanceTestCronJob(request);
    }

    public void createSchedule(ScheduleRequest request) {
        scheduleService.addSchedule(buildPerformanceTestSchedule(request));
        addOrUpdatePerformanceTestCronJob(request);
    }

    private Schedule buildPerformanceTestSchedule(ScheduleRequest request) {
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
            stopEngine(reportId);
            // 发送测试停止消息
            loadTestProducer.sendMessage(reportId);
            // 停止测试之后设置报告的状态
            reportService.updateStatus(reportId, PerformanceTestStatus.Completed.name());
        }
    }

    public void stopErrorTest(String reportId) {
        stopEngine(reportId);
        // 停止测试之后设置报告的状态
        reportService.updateStatus(reportId, PerformanceTestStatus.Error.name());
    }

    private void stopEngine(String reportId) {
        LoadTestReport loadTestReport = loadTestReportMapper.selectByPrimaryKey(reportId);
        LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(loadTestReport.getTestId());
        final Engine engine = EngineFactory.createEngine(loadTest);
        if (engine == null) {
            MSException.throwException(String.format("Stop report fail. create engine fail，report ID：%s", reportId));
        }
        reportService.stopEngine(loadTest, engine);
    }

    public List<ScheduleDao> listSchedule(QueryScheduleRequest request) {
        request.setEnable(true);
        List<ScheduleDao> schedules = scheduleService.list(request);
        List<String> resourceIds = schedules.stream()
                .map(Schedule::getResourceId)
                .collect(Collectors.toList());
        if (!resourceIds.isEmpty()) {
            LoadTestExample example = new LoadTestExample();
            LoadTestExample.Criteria criteria = example.createCriteria();
            if (StringUtils.isNotBlank(SessionUtils.getCurrentProjectId())) {
                criteria.andProjectIdEqualTo(SessionUtils.getCurrentProjectId());
            }
            criteria.andIdIn(resourceIds);
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

    public List<LoadTest> getLoadTestListByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        LoadTestExample loadTestExample = new LoadTestExample();
        loadTestExample.createCriteria().andIdIn(ids);
        List<LoadTest> loadTests = loadTestMapper.selectByExample(loadTestExample);
        return Optional.ofNullable(loadTests).orElse(new ArrayList<>());
    }

    private int getNextNum(String projectId) {
        LoadTest loadTest = extLoadTestMapper.getNextNum(projectId);
        if (loadTest == null) {
            return 100001;
        } else {
            return Optional.of(loadTest.getNum() + 1).orElse(100001);
        }
    }

    public List<FileMetadata> getProjectFiles(String projectId, String loadType) {
        List<String> loadTypes = new ArrayList<>();
        loadTypes.add(StringUtils.upperCase(loadType));
        if (StringUtils.equalsIgnoreCase(loadType, "resource")) {
            loadTypes.add(FileType.CSV.name());
            loadTypes.add(FileType.JAR.name());
        }
        if (StringUtils.equalsIgnoreCase(loadType, "all")) {
            loadTypes.add(FileType.CSV.name());
            loadTypes.add(FileType.JAR.name());
            loadTypes.add(FileType.JMX.name());
        }
        return extLoadTestMapper.getProjectFiles(projectId, loadTypes);
    }

    public List<LoadTestExportJmx> exportJmx(List<String> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return null;
        }
        List<LoadTestExportJmx> results = new ArrayList<>();
        fileIds.forEach(id -> {
            FileMetadata fileMetadata = fileService.getFileMetadataById(id);
            FileContent fileContent = fileService.getFileContent(id);
            results.add(new LoadTestExportJmx(fileMetadata.getName(), new String(fileContent.getFile(), StandardCharsets.UTF_8)));
        });

        return results;
    }

    public List<FileMetadata> getFileMetadataByTestId(String testId) {
        LoadTestFileExample loadTestFileExample = new LoadTestFileExample();
        loadTestFileExample.createCriteria().andTestIdEqualTo(testId);
        loadTestFileExample.setOrderByClause("sort asc");
        List<LoadTestFile> loadTestFiles = loadTestFileMapper.selectByExample(loadTestFileExample);

        List<String> fileIds = loadTestFiles.stream().map(LoadTestFile::getFileId).collect(Collectors.toList());
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(fileIds);
        return fileService.getFileMetadataByIds(fileIds);
    }
}
