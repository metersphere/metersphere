package io.metersphere.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.BaseProjectVersionMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportDetailMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.JSON;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.config.JmeterProperties;
import io.metersphere.config.KafkaProperties;
import io.metersphere.dto.*;
import io.metersphere.engine.Engine;
import io.metersphere.engine.EngineFactory;
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.PerformanceTestJob;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.performance.PerformanceReference;
import io.metersphere.metadata.service.FileMetadataService;
import io.metersphere.notice.service.NotificationService;
import io.metersphere.quota.service.BaseQuotaService;
import io.metersphere.request.*;
import io.metersphere.task.dto.TaskRequestDTO;
import io.metersphere.utils.JmxParseUtil;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
    private PerformanceReportService performanceReportService;
    @Resource
    private KafkaProperties kafkaProperties;
    @Resource
    private LoadTestFollowMapper loadTestFollowMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private BaseProjectVersionMapper baseProjectVersionMapper;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private LoadTestReportFileMapper loadTestReportFileMapper;
    @Resource
    private BaseScheduleService baseScheduleService;
    @Resource
    private FileMetadataService fileMetadataService;
    @Resource
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;
    @Resource
    private TestCaseTestMapper testCaseTestMapper;
    @Resource
    private BaseQuotaService baseQuotaService;
    @Resource
    private BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    private NotificationService notificationService;
    @Resource
    private BaseProjectService baseProjectService;

    public List<LoadTestDTO> list(QueryTestPlanRequest request) {
        request.setOrders(ServiceUtils.getDefaultSortOrder(request.getOrders()));
        return extLoadTestMapper.list(request);
    }

    public void delete(DeleteTestPlanRequest request) {

        LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(request.getId());
        LoadTestExample example = new LoadTestExample();
        example.createCriteria().andRefIdEqualTo(loadTest.getRefId());
        List<LoadTest> loadTests = loadTestMapper.selectByExample(example);

        loadTests.forEach(test -> {
            baseScheduleService.deleteByResourceId(test.getId(), ScheduleGroup.PERFORMANCE_TEST.name());

            // delete load_test
            loadTestMapper.deleteByPrimaryKey(test.getId());
            // delete test_plan_load_case
            TestPlanLoadCaseExample testPlanLoadCaseExample = new TestPlanLoadCaseExample();
            testPlanLoadCaseExample.createCriteria().andLoadCaseIdEqualTo(test.getId());
            testPlanLoadCaseMapper.deleteByExample(testPlanLoadCaseExample);
            // delete test_case_test
            TestCaseTestExample testCaseTestExample = new TestCaseTestExample();
            testCaseTestExample.createCriteria().andTestIdEqualTo(test.getId());
            testCaseTestMapper.deleteByExample(testCaseTestExample);

            detachFileByTestId(test.getId());

            deleteFollows(test.getId());
        });
    }

    private void deleteFollows(String testId) {
        LoadTestFollowExample example = new LoadTestFollowExample();
        example.createCriteria().andTestIdEqualTo(testId);
        loadTestFollowMapper.deleteByExample(example);
    }

    public void detachFileByTestId(String testId) {
        LoadTestFileExample loadTestFileExample = new LoadTestFileExample();
        loadTestFileExample.createCriteria().andTestIdEqualTo(testId);
        loadTestFileMapper.deleteByExample(loadTestFileExample);
    }

    public LoadTest save(SaveTestPlanRequest request, List<MultipartFile> files) {
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
        return loadTest;
    }

    private void conversionFiles(String id, List<String> conversionFileIdList) {
        for (String metaFileId : conversionFileIdList) {
            if (!this.loadTestFileExists(id, metaFileId)) {
                LoadTestFile loadTestFile = new LoadTestFile();
                loadTestFile.setTestId(id);
                loadTestFile.setFileId(metaFileId);
                loadTestFileMapper.insert(loadTestFile);
            }
        }
    }

    private boolean loadTestFileExists(String testId, String metaFileId) {
        boolean fileExists = fileMetadataService.isFileExits(metaFileId);
        LoadTestFileExample example = new LoadTestFileExample();
        example.createCriteria().andTestIdEqualTo(testId).andFileIdEqualTo(metaFileId);
        long loadTestFiles = loadTestFileMapper.countByExample(example);

        if (!fileExists && loadTestFiles > 0) {
            return false;
        } else {
            return true;
        }
    }

    private void saveUploadFiles(List<MultipartFile> files, LoadTest loadTest, Map<String, Integer> fileSorts) {
        if (files != null) {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                FileMetadata fileMetadata = fileMetadataService.saveFile(file, loadTest.getProjectId());
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
            FileMetadata fileMetadata = fileMetadataService.getFileMetadataById(fileId);
            LoadTestFile loadTestFile = new LoadTestFile();
            loadTestFile.setTestId(testId);
            loadTestFile.setFileId(fileId);
            loadTestFile.setSort(fileSorts.getOrDefault(fileMetadata.getName(), i));
            loadTestFileMapper.insert(loadTestFile);
        }
    }

    private void checkExist(TestPlanRequest request) {
        if (StringUtils.isEmpty(request.getVersionId())) {
            request.setVersionId(baseProjectVersionMapper.getDefaultVersion(request.getProjectId()));
        }
        if (request.getName() != null) {
            LoadTestExample example = new LoadTestExample();
            LoadTestExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId()).andVersionIdEqualTo(request.getVersionId());
            if (StringUtils.isNotBlank(request.getId())) {
                criteria.andIdNotEqualTo(request.getId());
            }
            if (loadTestMapper.selectByExample(example).size() > 0) {
                MSException.throwException(Translator.get("api_test_name_already_exists"));
            }
        }
    }

    private LoadTestWithBLOBs saveLoadTest(SaveTestPlanRequest request) {
        checkExist(request);

        final LoadTestWithBLOBs loadTest = new LoadTestWithBLOBs();
        loadTest.setUserId(SessionUtils.getUser().getId());
        loadTest.setId(request.getId());
        loadTest.setCreateUser(SessionUtils.getUserId());
        loadTest.setName(request.getName());
        loadTest.setProjectId(request.getProjectId());
        loadTest.setCreateTime(System.currentTimeMillis());
        loadTest.setUpdateTime(System.currentTimeMillis());
        loadTest.setTestResourcePoolId(request.getTestResourcePoolId());
        loadTest.setLoadConfiguration(request.getLoadConfiguration());
        loadTest.setAdvancedConfiguration(request.getAdvancedConfiguration());
        loadTest.setStatus(PerformanceTestStatus.Saved.name());
        loadTest.setNum(getNextNum(request.getProjectId()));
        if (MapUtils.isNotEmpty(request.getProjectEnvMap())) {
            loadTest.setEnvInfo(JSON.toJSONString(request.getProjectEnvMap()));
        }
        loadTest.setOrder(ServiceUtils.getNextOrder(request.getProjectId(), extLoadTestMapper::getLastOrder));
        loadTest.setVersionId(request.getVersionId());
        loadTest.setRefId(request.getId());
        loadTest.setLatest(true); // 创建新版本的时候一定是最新的
        loadTestMapper.insert(loadTest);
        saveFollows(loadTest.getId(), request.getFollows());
        return loadTest;
    }

    public LoadTest edit(EditTestPlanRequest request, List<MultipartFile> files) {
        checkExist(request);
        String testId = request.getId();
        LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(testId);
        if (loadTest == null) {
            MSException.throwException(Translator.get("edit_load_test_not_found") + testId);
        }
        if (StringUtils.containsAny(loadTest.getStatus(), PerformanceTestStatus.Running.name(), PerformanceTestStatus.Starting.name())) {
            MSException.throwException(Translator.get("cannot_edit_load_test_running"));
        }
        // 新选择了一个文件，删除原来的文件
        List<FileMetadata> updatedFiles = request.getUpdatedFileList();
        List<FileMetadata> originFiles = getFileMetadataByTestId(testId);
        List<String> updatedFileIds = updatedFiles.stream().map(FileMetadata::getId).collect(Collectors.toList());
        List<String> originFileIds = originFiles.stream().map(FileMetadata::getId).collect(Collectors.toList());

        // 相减
        List<String> deleteFileIds = ListUtils.subtract(originFileIds, updatedFileIds);
        // 删除已经不相关的文件
        if (!CollectionUtils.isEmpty(deleteFileIds)) {
            LoadTestFileExample example3 = new LoadTestFileExample();
            example3.createCriteria().andFileIdIn(deleteFileIds).andTestIdEqualTo(testId);
            loadTestFileMapper.deleteByExample(example3);
        }

        // 导入项目里其他的文件
        List<String> addFileIds = ListUtils.subtract(updatedFileIds, originFileIds);
        this.importFiles(addFileIds, loadTest.getId(), request.getFileSorts());
        // 处理新上传的文件
        this.saveUploadFiles(files, loadTest, request.getFileSorts());
        // 保持文件顺序
        updatedFiles.forEach(f -> {
            LoadTestFile record = new LoadTestFile();
            record.setSort(request.getFileSorts().get(f.getName()));
            LoadTestFileExample loadTestFileExample = new LoadTestFileExample();
            loadTestFileExample.createCriteria().andFileIdEqualTo(f.getId()).andTestIdEqualTo(testId);
            loadTestFileMapper.updateByExampleSelective(record, loadTestFileExample);
        });

        loadTest.setName(request.getName());
        loadTest.setProjectId(request.getProjectId());
        loadTest.setUpdateTime(System.currentTimeMillis());
        loadTest.setLoadConfiguration(request.getLoadConfiguration());
        loadTest.setAdvancedConfiguration(request.getAdvancedConfiguration());
        loadTest.setTestResourcePoolId(request.getTestResourcePoolId());
        loadTest.setStatus(PerformanceTestStatus.Saved.name());
        // 更新数据
        LoadTestExample example = new LoadTestExample();
        example.createCriteria().andIdEqualTo(loadTest.getId()).andVersionIdEqualTo(request.getVersionId());
        if (loadTestMapper.updateByExampleSelective(loadTest, example) == 0) {
            // 插入新版本的数据
            LoadTestWithBLOBs oldLoadTest = loadTestMapper.selectByPrimaryKey(loadTest.getId());
            loadTest.setId(UUID.randomUUID().toString());
            loadTest.setNum(oldLoadTest.getNum());
            loadTest.setVersionId(request.getVersionId());
            loadTest.setCreateTime(System.currentTimeMillis());
            loadTest.setUpdateTime(System.currentTimeMillis());
            loadTest.setCreateUser(SessionUtils.getUserId());
            loadTest.setOrder(oldLoadTest.getOrder());
            loadTest.setRefId(oldLoadTest.getRefId());
            if (oldLoadTest.getLatest()) {
                loadTest.setLatest(false);
            }
            //插入文件
            copyLoadTestFiles(testId, loadTest.getId());
            loadTestMapper.insertSelective(loadTest);
        }
        String defaultVersion = baseProjectVersionMapper.getDefaultVersion(request.getProjectId());
        if (StringUtils.equalsIgnoreCase(request.getVersionId(), defaultVersion)) {
            checkAndSetLatestVersion(loadTest.getRefId());
        }
        return loadTest;
    }

    /**
     * 检查设置最新版本
     */
    private void checkAndSetLatestVersion(String refId) {
        extLoadTestMapper.clearLatestVersion(refId);
        extLoadTestMapper.addLatestVersion(refId);
    }

    public void saveFollows(String testId, List<String> follows) {
        LoadTestFollowExample example = new LoadTestFollowExample();
        example.createCriteria().andTestIdEqualTo(testId);
        loadTestFollowMapper.deleteByExample(example);
        if (!CollectionUtils.isEmpty(follows)) {
            for (String follow : follows) {
                LoadTestFollow loadTestFollow = new LoadTestFollow();
                loadTestFollow.setTestId(testId);
                loadTestFollow.setFollowId(follow);
                loadTestFollowMapper.insert(loadTestFollow);
            }
        }
    }

    @Transactional(noRollbackFor = MSException.class)//  保存失败的信息
    public String run(RunTestPlanRequest request) {
        //检查性能测试文件
        this.checkLoadTestJmxFile(request.getId());
        LogUtil.info("性能测试run测试");
        final LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(request.getId());
        if (request.getUserId() != null) {
            loadTest.setUserId(request.getUserId());
        }
        if (StringUtils.isNotEmpty(request.getProjectId())) {
            loadTest.setProjectId(request.getProjectId());
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

        return startEngine(loadTest, request);
    }

    private void checkLoadTestJmxFile(String id) {
        LoadTestFileExample loadTestFileExample = new LoadTestFileExample();
        loadTestFileExample.createCriteria().andTestIdEqualTo(id);
        List<LoadTestFile> loadTestFileList = loadTestFileMapper.selectByExample(loadTestFileExample);
        List<String> fileIdList = loadTestFileList.stream().map(LoadTestFile::getFileId).collect(Collectors.toList());

        boolean hasJmx = false;
        for (String fileId : fileIdList) {
            FileMetadataWithBLOBs fileMetadata = fileMetadataService.selectById(fileId);
            if (fileMetadata != null && StringUtils.equalsIgnoreCase(fileMetadata.getType(), "jmx")) {
                hasJmx = true;
                byte[] bytes = fileMetadataService.loadFileAsBytes(fileMetadata);
                if (bytes == null || bytes.length == 0) {
                    MSException.throwException(Translator.get("load_test_file_is_not_jmx"));
                }
            }
        }
        if (!hasJmx) {
            MSException.throwException(Translator.get("load_test_file_not_have_jmx"));
        }
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
                try (Socket soc = new Socket();) {
                    soc.connect(new InetSocketAddress(ip, port), 1000); // 1s timeout
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("load_test_kafka_invalid"));
        }
    }

    private String startEngine(LoadTestWithBLOBs loadTest, RunTestPlanRequest request) {


        LoadTestReportWithBLOBs testReport = new LoadTestReportWithBLOBs();
        testReport.setId(StringUtils.isNotEmpty(request.getReportId()) ? request.getReportId() : UUID.randomUUID().toString());
        testReport.setCreateTime(System.currentTimeMillis());
        testReport.setUpdateTime(System.currentTimeMillis());
        testReport.setTestId(loadTest.getId());
        testReport.setName(loadTest.getName());
        testReport.setTriggerMode(request.getTriggerMode());
        testReport.setVersionId(loadTest.getVersionId());
        if (SessionUtils.getUser() == null) {
            testReport.setUserId(loadTest.getUserId());
        } else {
            testReport.setUserId(SessionUtils.getUser().getId());
        }
        // 只更新已设置的属性，其它未设置属性不更新
        LoadTestWithBLOBs updateTest = new LoadTestWithBLOBs();
        updateTest.setId(loadTest.getId());
        // 启动测试
        Engine engine = null;
        try {
            // 保存测试里的配置
            testReport.setTestResourcePoolId(loadTest.getTestResourcePoolId());
            testReport.setLoadConfiguration(loadTest.getLoadConfiguration());
            testReport.setAdvancedConfiguration(loadTest.getAdvancedConfiguration());
            // 如果request带了配置，覆盖掉loadtest上的
            if (StringUtils.isNotBlank(request.getLoadConfiguration())) {
                testReport.setLoadConfiguration(request.getLoadConfiguration());
            }
            if (StringUtils.isNotBlank(request.getAdvancedConfiguration())) {
                testReport.setAdvancedConfiguration(request.getAdvancedConfiguration());
            }
            if (StringUtils.isNotBlank(request.getTestResourcePoolId())) {
                testReport.setTestResourcePoolId(request.getTestResourcePoolId());
            }

            testReport.setStatus(PerformanceTestStatus.Starting.name());
            testReport.setProjectId(loadTest.getProjectId());
            testReport.setTestName(loadTest.getName());
            testReport.setEnvInfo(loadTest.getEnvInfo());
            // 启动插入 report
            loadTestReportMapper.insertSelective(testReport);

            // engine
            engine = EngineFactory.createEngine(testReport);
            if (engine == null) {
                MSException.throwException(String.format("Test cannot be run，test ID：%s", loadTest.getId()));
            }

            updateTest.setStatus(PerformanceTestStatus.Starting.name());
            loadTestMapper.updateByPrimaryKeySelective(updateTest);

            LoadTestReportDetail reportDetail = new LoadTestReportDetail();
            reportDetail.setContent(HEADERS);
            reportDetail.setReportId(testReport.getId());
            reportDetail.setPart(1L);
            loadTestReportDetailMapper.insertSelective(reportDetail);
            // append \n
            extLoadTestReportDetailMapper.appendLine(testReport.getId(), StringUtils.LF);
            // 保存一个 reportStatus
            LoadTestReportResult reportResult = new LoadTestReportResult();
            reportResult.setId(UUID.randomUUID().toString());
            reportResult.setReportId(testReport.getId());
            reportResult.setReportKey(ReportKeys.ResultStatus.name());
            reportResult.setReportValue("Ready"); // 初始化一个 result_status, 这个值用在data-streaming中
            loadTestReportResultMapper.insertSelective(reportResult);
            // 保存标记
            LoadTestReportResult rr = new LoadTestReportResult();
            rr.setId(UUID.randomUUID().toString());
            rr.setReportId(testReport.getId());
            rr.setReportKey(ReportKeys.VumProcessedStatus.name());
            rr.setReportValue(VumProcessedStatus.NOT_PROCESSED); // 避免测试运行出错时，对报告重复处理vum_used值
            loadTestReportResultMapper.insertSelective(rr);
            // 保存执行当时的file
            saveLoadTestReportFiles(testReport);
            // 检查配额
            this.checkLoadQuota(testReport, engine);
            return testReport.getId();
        } catch (MSException e) {
            // 启动失败之后清理任务
            if (engine != null) {
                engine.stop();
            }
            LogUtil.error(e.getMessage(), e);
            updateTest.setStatus(PerformanceTestStatus.Error.name());
            updateTest.setDescription(e.getMessage());
            loadTestMapper.updateByPrimaryKeySelective(updateTest);
            loadTestReportMapper.deleteByPrimaryKey(testReport.getId());
            throw e;
        }
    }

    private void saveLoadTestReportFiles(LoadTestReport report) {
        LoadTestFileExample example = new LoadTestFileExample();
        example.createCriteria().andTestIdEqualTo(report.getTestId());
        List<LoadTestFile> files = loadTestFileMapper.selectByExample(example);
        files.forEach(f -> {
            LoadTestReportFile record = new LoadTestReportFile();
            record.setFileId(f.getFileId());
            record.setSort(f.getSort());
            record.setReportId(report.getId());
            loadTestReportFileMapper.insert(record);
        });
    }

    private void checkLoadQuota(LoadTestReportWithBLOBs testReport, Engine engine) {
        RunTestPlanRequest checkRequest = new RunTestPlanRequest();
        checkRequest.setLoadConfiguration(testReport.getLoadConfiguration());
        checkRequest.setProjectId(testReport.getProjectId());
        baseQuotaService.checkLoadTestQuota(checkRequest, false);
        String projectId = testReport.getProjectId();
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null || StringUtils.isBlank(project.getWorkspaceId())) {
            MSException.throwException("project is null or workspace_id of project is null. project id: " + projectId);
        }
        RLock lock = redissonClient.getLock(project.getWorkspaceId());
        try {
            lock.lock();
            BigDecimal toUsed = baseQuotaService.checkVumUsed(checkRequest, projectId);
            engine.start();
            if (toUsed.compareTo(BigDecimal.ZERO) != 0) {
                baseQuotaService.updateVumUsed(projectId, toUsed);
            }
        } finally {
            lock.unlock();
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
            Schedule schedule = baseScheduleService.getScheduleByResource(loadTestDTO.getId(), ScheduleGroup.PERFORMANCE_TEST.name());
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
                byte[] content = fileMetadataService.loadFileAsBytes(metadata.getId());
                results.add(new LoadTestExportJmx(metadata.getName(), new String(content, StandardCharsets.UTF_8)));
            }
        }
        return results;
    }

    public List<DashboardTestDTO> dashboardTests(String projectId) {
        Instant oneYearAgo = Instant.now().plus(-365, ChronoUnit.DAYS);
        long startTimestamp = oneYearAgo.toEpochMilli();
        return extLoadTestReportMapper.selectDashboardTests(projectId, startTimestamp);
    }

    public List<LoadTest> getLoadTestByProjectId(String projectId) {
        return extLoadTestMapper.getLoadTestByProjectId(projectId);
    }

    public LoadTest getLoadTestBytestId(String testId) {
        return loadTestMapper.selectByPrimaryKey(testId);
    }

    public void copy(SaveTestPlanRequest request) {
        LoadTestWithBLOBs copy = loadTestMapper.selectByPrimaryKey(request.getId());
        request.setProjectId(copy.getProjectId());
        checkQuota(request, true);
        // copy test
        String copyName = copy.getName() + "_" + RandomStringUtils.randomAlphanumeric(5);

        if (StringUtils.length(copyName) > 255) {
            MSException.throwException(Translator.get("load_test_name_length"));
        }
        copy.setId(UUID.randomUUID().toString());
        copy.setName(copyName);
        copy.setCreateTime(System.currentTimeMillis());
        copy.setUpdateTime(System.currentTimeMillis());
        copy.setStatus(APITestStatus.Saved.name());
        copy.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        copy.setCreateUser(Objects.requireNonNull(SessionUtils.getUser()).getId());
        copy.setNum(getNextNum(copy.getProjectId()));
        copy.setRefId(copy.getId());
        copy.setLatest(true);
        loadTestMapper.insert(copy);
        // copy test file
        copyLoadTestFiles(request.getId(), copy.getId());
        request.setId(copy.getId());
    }

    private void copyLoadTestFiles(String oldLoadTestId, String newLoadTestId) {
        LoadTestFileExample loadTestFileExample = new LoadTestFileExample();
        loadTestFileExample.createCriteria().andTestIdEqualTo(oldLoadTestId);
        List<LoadTestFile> loadTestFiles = loadTestFileMapper.selectByExample(loadTestFileExample);
        if (!CollectionUtils.isEmpty(loadTestFiles)) {
            loadTestFiles.forEach(loadTestFile -> {
                loadTestFile.setTestId(newLoadTestId);
                loadTestFileMapper.insert(loadTestFile);
            });
        }
    }

    public void updateSchedule(Schedule request) {
        baseScheduleService.editSchedule(request);
        addOrUpdatePerformanceTestCronJob(request);
    }

    public void createSchedule(ScheduleRequest request) {
        baseScheduleService.addSchedule(buildPerformanceTestSchedule(request));
        addOrUpdatePerformanceTestCronJob(request);
    }

    private Schedule buildPerformanceTestSchedule(ScheduleRequest request) {
        Schedule schedule = baseScheduleService.buildApiTestSchedule(request);
        schedule.setJob(PerformanceTestJob.class.getName());
        schedule.setGroup(ScheduleGroup.PERFORMANCE_TEST.name());
        schedule.setType(ScheduleType.CRON.name());
        return schedule;
    }

    private void addOrUpdatePerformanceTestCronJob(Schedule request) {
        baseScheduleService.addOrUpdateCronJob(request, PerformanceTestJob.getJobKey(request.getResourceId()), PerformanceTestJob.getTriggerKey(request.getResourceId()), PerformanceTestJob.class);
    }

    public void stopTest(String reportId, boolean forceStop) {
        if (forceStop) {
            performanceReportService.deleteReport(reportId);
        } else {
            stopEngineHandleVum(reportId);
            // 停止测试之后设置报告的状态
            performanceReportService.updateStatus(reportId, PerformanceTestStatus.Completed.name());
        }
    }

    public void stopErrorTest(String reportId) {
        stopEngine(reportId);
        // 停止测试之后设置报告的状态
        performanceReportService.updateStatus(reportId, PerformanceTestStatus.Error.name());
    }

    private void stopEngine(String reportId) {
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(reportId);
        LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(loadTestReport.getTestId());
        final Engine engine = EngineFactory.createEngine(loadTestReport);
        if (engine == null) {
            MSException.throwException(String.format("Stop report fail. create engine fail，report ID：%s", reportId));
        }
        performanceReportService.stopEngine(loadTest, engine);
    }

    private void stopEngineHandleVum(String reportId) {
        LoadTestReportWithBLOBs loadTestReport = loadTestReportMapper.selectByPrimaryKey(reportId);
        final Engine engine = EngineFactory.createEngine(loadTestReport);
        if (engine == null) {
            MSException.throwException(String.format("Stop report fail. create engine fail，report ID：%s", reportId));
        }
        performanceReportService.stopEngineHandleVum(loadTestReport, engine);
    }

    public List<ScheduleDao> listSchedule(QueryScheduleRequest request) {
        request.setEnable(true);
        List<ScheduleDao> schedules = baseScheduleService.list(request);
        List<String> resourceIds = schedules.stream().map(Schedule::getResourceId).collect(Collectors.toList());
        if (!resourceIds.isEmpty()) {
            LoadTestExample example = new LoadTestExample();
            LoadTestExample.Criteria criteria = example.createCriteria();
            if (StringUtils.isNotBlank(request.getProjectId())) {
                criteria.andProjectIdEqualTo(request.getProjectId());
            }
            criteria.andIdIn(resourceIds);
            List<LoadTest> loadTests = loadTestMapper.selectByExample(example);
            Map<String, String> loadTestMap = loadTests.stream().collect(Collectors.toMap(LoadTest::getId, LoadTest::getName));
            baseScheduleService.build(loadTestMap, schedules);
        }
        return schedules;
    }

    private void checkQuota(TestPlanRequest request, boolean create) {
        baseQuotaService.checkLoadTestQuota(request, create);

    }

    public List<LoadTestDTO> getLoadTestListByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        QueryTestPlanRequest request = new QueryTestPlanRequest();
        Map filters = new HashMap();
        filters.put("id", ids);
        request.setFilters(filters);
        List<LoadTestDTO> loadTestDTOS = extLoadTestMapper.list(request);
        return loadTestDTOS;
    }

    private int getNextNum(String projectId) {
        LoadTest loadTest = extLoadTestMapper.getNextNum(projectId);
        if (loadTest == null || loadTest.getNum() == null) {
            return 100001;
        } else {
            return Optional.of(loadTest.getNum() + 1).orElse(100001);
        }
    }

    public List<FileMetadata> getProjectFiles(String projectId, String loadType, QueryProjectFileRequest request) {
        List<String> loadTypes = new ArrayList<>();
        loadTypes.add(StringUtils.upperCase(loadType));
        if (StringUtils.equalsIgnoreCase(loadType, "resource")) {
            List<String> fileTypes = Arrays.stream(FileType.values()).filter(fileType -> !fileType.equals(FileType.JMX)).map(FileType::name).collect(Collectors.toList());
            loadTypes.addAll(fileTypes);
        }
        if (StringUtils.equalsIgnoreCase(loadType, "all")) {
            List<String> fileTypes = Arrays.stream(FileType.values()).map(FileType::name).collect(Collectors.toList());
            loadTypes.addAll(fileTypes);
        }
        return extLoadTestMapper.getProjectFiles(projectId, loadTypes, request);
    }

    public List<LoadTestExportJmx> exportJmx(List<String> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return null;
        }
        List<LoadTestExportJmx> results = new ArrayList<>();
        fileIds.forEach(id -> {
            FileMetadata fileMetadata = fileMetadataService.getFileMetadataById(id);
            byte[] content = fileMetadataService.loadFileAsBytes(id);
            results.add(new LoadTestExportJmx(fileMetadata.getName(), new String(content, StandardCharsets.UTF_8)));
        });

        return results;
    }

    public List<FileMetadata> getFileMetadataByTestId(String testId) {
        return extLoadTestMapper.getFileMetadataByIds(testId);
    }

    public Long getReportCountByTestId(String testId) {
        LoadTestReportExample example = new LoadTestReportExample();
        example.createCriteria().andTestIdEqualTo(testId);
        return loadTestReportMapper.countByExample(example);
    }

    public String getLogDetails(String id) {
        LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(id);
        if (loadTest != null) {
            String loadConfiguration = loadTest.getLoadConfiguration();
            if (StringUtils.isNotEmpty(loadConfiguration)) {
                loadConfiguration = "{\"" + "压力配置" + "\":" + loadConfiguration + "}";
            }
            loadTest.setLoadConfiguration(loadConfiguration);
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(loadTest, PerformanceReference.performanceColumns);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(loadTest.getId()), loadTest.getProjectId(), loadTest.getName(), loadTest.getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String getRunLogDetails(String id) {
        LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(id);
        if (loadTest != null) {
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(loadTest.getId()), loadTest.getProjectId(), loadTest.getName(), loadTest.getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }

    public String deleteBatchLog(DeletePerformanceRequest request) {
        ServiceUtils.getSelectAllIds(request, request, (query) -> getLoadTestIds(request.getProjectId()));
        List<String> loadTestIds = request.getIds();
        LoadTestExample example = new LoadTestExample();
        example.createCriteria().andIdIn(loadTestIds);
        List<LoadTest> tests = loadTestMapper.selectByExample(example);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(tests)) {
            List<String> names = tests.stream().map(LoadTest::getName).collect(Collectors.toList());
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(loadTestIds), tests.get(0).getProjectId(), String.join(",", names), tests.get(0).getCreateUser(), new LinkedList<>());
            return JSON.toJSONString(details);
        }
        return null;
    }


    public Integer getGranularity(String reportId) {
        Integer granularity = CommonBeanFactory.getBean(JmeterProperties.class).getReport().getGranularity();
        try {
            LoadTestReportWithBLOBs report = loadTestReportMapper.selectByPrimaryKey(reportId);
            Map advancedConfig = JSON.parseObject(report.getAdvancedConfiguration(), Map.class);
            if (advancedConfig.get("granularity") != null && StringUtils.isNotEmpty(advancedConfig.get("granularity").toString())) {
                return (int) advancedConfig.get("granularity") * 1000;// 单位是ms
            }
            AtomicReference<Integer> maxDuration = new AtomicReference<>(0);
            List<List<Map>> pressureConfigLists = JSON.parseArray(report.getLoadConfiguration());
            // 按照最长的执行时间来确定
            pressureConfigLists.forEach(pcList -> {

                Optional<Integer> maxOp = pcList.stream().filter(pressureConfig -> StringUtils.equalsAnyIgnoreCase((String) pressureConfig.get("key"), "hold", "duration")).map(pressureConfig -> (int) pressureConfig.get("value")).max(Comparator.naturalOrder());
                Integer max = maxOp.orElse(0);
                if (maxDuration.get() < max) {
                    maxDuration.set(max);
                }
            });
            Optional<GranularityData.Data> dataOptional = GranularityData.dataList.stream().filter(data -> maxDuration.get() >= data.getStart() && maxDuration.get() <= data.getEnd()).findFirst();

            if (dataOptional.isPresent()) {
                GranularityData.Data data = dataOptional.get();
                granularity = data.getGranularity();
            }

        } catch (Exception e) {
            LogUtil.error(e);
        }
        return granularity;
    }

    public List<LoadTest> getLoadCaseByIds(List<String> ids) {
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(ids)) {
            LoadTestExample example = new LoadTestExample();
            example.createCriteria().andIdIn(ids).andStatusNotEqualTo("Trash");
            return loadTestMapper.selectByExample(example);
        }
        return new ArrayList<>();
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(LoadTestWithBLOBs.class, LoadTestMapper.class, extLoadTestMapper::selectProjectIds, extLoadTestMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, LoadTestWithBLOBs.class, loadTestMapper::selectByPrimaryKey, extLoadTestMapper::getPreOrder, extLoadTestMapper::getLastOrder, loadTestMapper::updateByPrimaryKeySelective);
    }

    public List<LoadTestReportWithBLOBs> selectReportsByTestResourcePoolId(String resourcePoolId) {
        LoadTestReportExample example = new LoadTestReportExample();
        example.createCriteria().andTestResourcePoolIdEqualTo(resourcePoolId).andStatusIn(Arrays.asList(PerformanceTestStatus.Running.name(), PerformanceTestStatus.Starting.name()));
        return loadTestReportMapper.selectByExampleWithBLOBs(example);
    }

    public List<String> getFollows(String testId) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(testId)) {
            return result;
        }
        LoadTestFollowExample example = new LoadTestFollowExample();
        example.createCriteria().andTestIdEqualTo(testId);
        List<LoadTestFollow> follows = loadTestFollowMapper.selectByExample(example);
        return follows.stream().map(LoadTestFollow::getFollowId).distinct().collect(Collectors.toList());
    }


    public List<LoadTestDTO> getLoadTestVersions(String loadTestId) {
        LoadTestWithBLOBs loadTestWithBLOBs = loadTestMapper.selectByPrimaryKey(loadTestId);
        if (loadTestWithBLOBs == null) {
            return new ArrayList<>();
        }
        QueryTestPlanRequest request = new QueryTestPlanRequest();
        request.setRefId(loadTestWithBLOBs.getRefId());
        return this.list(request);
    }

    public LoadTestDTO getLoadTestByVersion(String versionId, String refId) {
        QueryTestPlanRequest request = new QueryTestPlanRequest();
        request.setRefId(refId);
        request.setVersionId(versionId);
        List<LoadTestDTO> list = this.list(request);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public void deleteLoadTestByVersion(String version, String refId) {
        LoadTestExample loadTestExample = new LoadTestExample();
        loadTestExample.createCriteria().andRefIdEqualTo(refId).andVersionIdEqualTo(version);
        List<LoadTest> loadTests = loadTestMapper.selectByExample(loadTestExample);
        if (CollectionUtils.isEmpty(loadTests)) {
            return;
        }
        loadTestMapper.deleteByExample(loadTestExample);
        checkAndSetLatestVersion(refId);
    }

    public void deleteBatch(DeletePerformanceRequest request) {
        ServiceUtils.getSelectAllIds(request, request, (query) -> getLoadTestIds(request.getProjectId()));
        List<String> loadTestIds = request.getIds();
        loadTestIds.forEach(id -> {
            DeleteTestPlanRequest rq = new DeleteTestPlanRequest();
            rq.setForceDelete(true);
            rq.setId(id);
            this.delete(rq);
        });
    }

    private List<String> getLoadTestIds(String projectId) {
        List<LoadTest> loadTests = this.getLoadTestByProjectId(projectId);
        return loadTests.stream().map(LoadTest::getId).collect(Collectors.toList());
    }


    public List<LoadTestDTO> listBatch(LoadTestBatchRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(), (query) -> extLoadTestMapper.selectIds(query));
        if (org.apache.commons.collections.CollectionUtils.isEmpty(request.getIds())) {
            return new ArrayList<>();
        }

        QueryTestPlanRequest request2 = new QueryTestPlanRequest();
        Map<String, List<String>> param = new HashMap<>();
        param.put("id", request.getIds());
        request2.setFilters(param);
        request2.setVersionId(request.getCondition().getVersionId());
        return this.list(request2);
    }

    public List<LoadTest> getLoadTestByPoolId(String poolId) {
        LoadTestExample loadTestExample = new LoadTestExample();
        loadTestExample.createCriteria().andTestResourcePoolIdEqualTo(poolId);
        return loadTestMapper.selectByExample(loadTestExample);
    }

    public void stopBatch(TaskRequestDTO taskRequestDTO) {
        if (StringUtils.isNotEmpty(taskRequestDTO.getReportId())) {
            this.stopTest(taskRequestDTO.getReportId(), false);
        } else {
            List<LoadTestReport> loadTestReports = extLoadTestReportMapper.selectReportByProjectId(taskRequestDTO.getProjectId());
            loadTestReports.forEach(loadTestReport -> {
                this.stopTest(loadTestReport.getId(), false);
            });
        }
    }

    public List<LoadCaseCountChartResult> countByRequest(LoadCaseCountRequest request) {
        return extLoadTestMapper.countByRequest(request);
    }

    public List<LoadTestFileDTO> selectLoadTestFileByRequest(LoadTestBatchRequest request) {
        if (request != null && CollectionUtils.isNotEmpty(request.getIds())) {
            List<LoadTestFileDTO> returnList = new ArrayList<>();
            LoadTestFileExample loadTestFileExample = new LoadTestFileExample();
            loadTestFileExample.createCriteria().andFileIdIn(request.getIds());
            List<LoadTestFile> loadTestFileList = loadTestFileMapper.selectByExample(loadTestFileExample);
            loadTestFileList.forEach(item -> {
                LoadTest loadTest = loadTestMapper.selectByPrimaryKey(item.getTestId());
                LoadTestFileDTO dto = new LoadTestFileDTO();
                dto.setTestId(item.getTestId());
                dto.setFileId(item.getFileId());
                dto.setSort(item.getSort());
                dto.setLoadTest(loadTest);
                loadTestFileList.add(dto);
            });

            return returnList;
        }
        return new ArrayList<>();

    }

    public void checkFileIsRelated(String fileId) {
        LoadTestFileExample example1 = new LoadTestFileExample();
        example1.createCriteria().andFileIdEqualTo(fileId);
        List<LoadTestFile> loadTestFiles = loadTestFileMapper.selectByExample(example1);
        String errorMessage = StringUtils.EMPTY;
        if (loadTestFiles.size() > 0) {
            List<String> testIds = loadTestFiles.stream().map(LoadTestFile::getTestId).distinct().collect(Collectors.toList());
            LoadTestExample example = new LoadTestExample();
            example.createCriteria().andIdIn(testIds);
            List<LoadTest> loadTests = loadTestMapper.selectByExample(example);
            errorMessage += Translator.get("load_test") + ": " + StringUtils.join(loadTests.stream().map(LoadTest::getName).toArray(), ",");
            errorMessage += StringUtils.LF;
        }
        if (StringUtils.isNotBlank(errorMessage)) {
            MSException.throwException(errorMessage + Translator.get("project_file_in_use"));
        }
    }

    public List<BaseCase> getBaseCaseByProjectId(String projectId) {
        return extLoadTestMapper.selectBaseCaseByProjectId(projectId);
    }

    private boolean checkLoadTest(LoadTest loadTest) {
        return loadTest != null && StringUtils.isNoneBlank(loadTest.getId(), loadTest.getName(), loadTest.getProjectId(), loadTest.getCreateUser());
    }

    //检查并发送脚本审核的通知
    public void checkAndSendReviewMessage(List<FileMetadata> fileMetadataList, List<MultipartFile> files, LoadTest loadTest) {
        if (checkLoadTest(loadTest)) {
            String projectId = loadTest.getProjectId();
            ProjectApplication reviewLoadTestScript = baseProjectApplicationService.getProjectApplication(
                    projectId, ProjectApplicationType.PERFORMANCE_REVIEW_LOAD_TEST_SCRIPT.name());
            if (BooleanUtils.toBoolean(reviewLoadTestScript.getTypeValue())) {
                boolean isSend = this.isSendScriptReviewMessage(fileMetadataList, files);
                if (isSend) {
                    String sendUser = loadTest.getCreateUser();
                    ProjectApplication loadTestScriptReviewerConfig = baseProjectApplicationService.getProjectApplication(projectId, ProjectApplicationType.PERFORMANCE_SCRIPT_REVIEWER.name());
                    if (StringUtils.isNotEmpty(loadTestScriptReviewerConfig.getTypeValue())) {
                        sendUser = loadTestScriptReviewerConfig.getTypeValue();
                    }
                    if (baseProjectService.isProjectMember(projectId, sendUser)) {
                        Notification notification = new Notification();
                        notification.setTitle("性能测试通知");
                        notification.setOperator(SessionUtils.getUserId());
                        notification.setOperation(NoticeConstants.Event.REVIEW);
                        notification.setResourceId(loadTest.getId());
                        notification.setResourceName(loadTest.getName());
                        notification.setResourceType(NoticeConstants.TaskType.PERFORMANCE_TEST_TASK);
                        notification.setType(NotificationConstants.Type.SYSTEM_NOTICE.name());
                        notification.setStatus(NotificationConstants.Status.UNREAD.name());
                        notification.setCreateTime(System.currentTimeMillis());
                        notification.setReceiver(sendUser);
                        notificationService.sendAnnouncement(notification);
                    }
                }
            }
        }

    }

    private boolean isSendScriptReviewMessage(List<FileMetadata> fileMetadataList, List<MultipartFile> files) {
        List<FileMetadataWithBLOBs> fileMetadataWithBLOBsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fileMetadataList)) {
            List<String> idList = fileMetadataList.stream().map(FileMetadata::getId).toList();
            fileMetadataWithBLOBsList = fileMetadataService.selectByIdAndType(idList, "JMX");
        }
        return JmxParseUtil.isJmxHasScriptByFiles(files) || JmxParseUtil.isJmxHasScriptByStorage(fileMetadataWithBLOBsList);
    }
}
