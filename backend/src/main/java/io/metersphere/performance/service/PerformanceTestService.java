package io.metersphere.performance.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import io.metersphere.api.dto.JmxInfoDTO;
import io.metersphere.api.dto.automation.ApiScenarioBatchRequest;
import io.metersphere.api.dto.automation.ApiScenarioExportJmxDTO;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiTestCaseService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportDetailMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.base.mapper.ext.ExtProjectVersionMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.config.JmeterProperties;
import io.metersphere.config.KafkaProperties;
import io.metersphere.controller.request.OrderRequest;
import io.metersphere.controller.request.QueryScheduleRequest;
import io.metersphere.controller.request.ResetOrderRequest;
import io.metersphere.controller.request.ScheduleRequest;
import io.metersphere.dto.DashboardTestDTO;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.dto.ScheduleDao;
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.PerformanceTestJob;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.performance.PerformanceReference;
import io.metersphere.performance.base.GranularityData;
import io.metersphere.performance.dto.LoadModuleDTO;
import io.metersphere.performance.dto.LoadTestExportJmx;
import io.metersphere.performance.engine.Engine;
import io.metersphere.performance.engine.EngineFactory;
import io.metersphere.performance.request.*;
import io.metersphere.service.ApiPerformanceService;
import io.metersphere.service.FileService;
import io.metersphere.service.QuotaService;
import io.metersphere.service.ScheduleService;
import io.metersphere.track.request.testplan.LoadCaseRequest;
import io.metersphere.track.service.TestCaseService;
import io.metersphere.track.service.TestPlanLoadCaseService;
import io.metersphere.track.service.TestPlanProjectService;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.aspectj.util.FileUtil;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
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
    private PerformanceReportService performanceReportService;
    @Resource
    private KafkaProperties kafkaProperties;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private TestCaseService testCaseService;
    @Resource
    private LoadTestFollowMapper loadTestFollowMapper;
    @Resource
    private ApiAutomationService apiAutomationService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ApiPerformanceService apiPerformanceService;
    @Lazy
    @Resource
    private TestPlanLoadCaseService testPlanLoadCaseService;
    @Resource
    private TestPlanLoadCaseMapper testPlanLoadCaseMapper;
    @Resource
    private TestPlanProjectService testPlanProjectService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ExtProjectVersionMapper extProjectVersionMapper;

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
            if (!request.isForceDelete()) {
                testCaseService.checkIsRelateTest(test.getId());
            }
            // 删除时保存jmx内容
            List<FileMetadata> fileMetadataList = getFileMetadataByTestId(test.getId());
            List<FileMetadata> jmxFiles = fileMetadataList.stream().filter(f -> StringUtils.equalsIgnoreCase(f.getType(), FileType.JMX.name())).collect(Collectors.toList());
            byte[] bytes = EngineFactory.mergeJmx(jmxFiles);
            LoadTestReportExample loadTestReportExample = new LoadTestReportExample();
            loadTestReportExample.createCriteria().andTestIdEqualTo(test.getId());
            List<LoadTestReport> loadTestReports = loadTestReportMapper.selectByExample(loadTestReportExample);
            loadTestReports.forEach(loadTestReport -> {
                LoadTestReportWithBLOBs record = new LoadTestReportWithBLOBs();
                record.setId(loadTestReport.getId());
                record.setJmxContent(new String(bytes, StandardCharsets.UTF_8));
                extLoadTestReportMapper.updateJmxContentIfAbsent(record);
            });
            //delete scheduleFunctionalCases
            scheduleService.deleteByResourceId(test.getId(), ScheduleGroup.PERFORMANCE_TEST.name());

            // delete load_test
            loadTestMapper.deleteByPrimaryKey(test.getId());

            testPlanLoadCaseService.deleteByTestId(test.getId());

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

    private void checkExist(TestPlanRequest request) {
        if (StringUtils.isEmpty(request.getVersionId())) {
            request.setVersionId(extProjectVersionMapper.getDefaultVersion(request.getProjectId()));
        }
        if (request.getName() != null) {
            LoadTestExample example = new LoadTestExample();
            LoadTestExample.Criteria criteria = example.createCriteria();
            criteria.andNameEqualTo(request.getName())
                    .andProjectIdEqualTo(request.getProjectId()).andVersionIdEqualTo(request.getVersionId());
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
        loadTest.setOrder(ServiceUtils.getNextOrder(request.getProjectId(), extLoadTestMapper::getLastOrder));
        loadTest.setVersionId(request.getVersionId());
        loadTest.setRefId(request.getId());
        loadTest.setLatest(true); // 创建新版本的时候一定是最新的
        List<ApiLoadTest> apiList = request.getApiList();
        apiPerformanceService.add(apiList, loadTest.getId());
        loadTestMapper.insert(loadTest);
        saveFollows(loadTest.getId(), request.getFollows());
        return loadTest;
    }

    public LoadTest edit(EditTestPlanRequest request, List<MultipartFile> files) {
        checkQuota(request, false);
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
            example3.createCriteria()
                    .andFileIdIn(deleteFileIds)
                    .andTestIdEqualTo(testId);
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
            loadTestFileExample.createCriteria()
                    .andFileIdEqualTo(f.getId())
                    .andTestIdEqualTo(testId);
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
            //插入文件
            copyLoadTestFiles(testId, loadTest.getId());
            loadTestMapper.insertSelective(loadTest);
        }
        checkAndSetLatestVersion(loadTest.getRefId());
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

    private void checkKafka() {
        String bootstrapServers = kafkaProperties.getBootstrapServers();
        String[] servers = StringUtils.split(bootstrapServers, ",");
        try {
            for (String s : servers) {
                String[] ipAndPort = s.split(":");
                //1,建立tcp
                String ip = ipAndPort[0];
                int port = Integer.parseInt(ipAndPort[1]);
                try (
                        Socket soc = new Socket();
                ) {
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
        testReport.setId(UUID.randomUUID().toString());
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
            // 启动插入 report
            testReport.setAdvancedConfiguration(loadTest.getAdvancedConfiguration());
            String testPlanLoadId = request.getTestPlanLoadId();
            if (StringUtils.isNotBlank(testPlanLoadId)) {
                // 设置本次报告中的压力配置信息
                TestPlanLoadCaseWithBLOBs testPlanLoadCase = testPlanLoadCaseMapper.selectByPrimaryKey(testPlanLoadId);
                if (testPlanLoadCase != null && StringUtils.isNotBlank(testPlanLoadCase.getLoadConfiguration())) {
                    testReport.setLoadConfiguration(testPlanLoadCase.getLoadConfiguration());
                }
                if (testPlanLoadCase != null && StringUtils.isNotBlank(testPlanLoadCase.getAdvancedConfiguration())) {
                    testReport.setAdvancedConfiguration(testPlanLoadCase.getAdvancedConfiguration());
                }
                if (testPlanLoadCase != null && StringUtils.isNotBlank(testPlanLoadCase.getTestResourcePoolId())) {
                    testReport.setTestResourcePoolId(testPlanLoadCase.getTestResourcePoolId());
                }
            }
            testReport.setStatus(PerformanceTestStatus.Starting.name());
            testReport.setProjectId(loadTest.getProjectId());
            testReport.setTestName(loadTest.getName());
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
            extLoadTestReportDetailMapper.appendLine(testReport.getId(), "\n");
            // 保存一个 reportStatus
            LoadTestReportResult reportResult = new LoadTestReportResult();
            reportResult.setId(UUID.randomUUID().toString());
            reportResult.setReportId(testReport.getId());
            reportResult.setReportKey(ReportKeys.ResultStatus.name());
            reportResult.setReportValue("Ready"); // 初始化一个 result_status, 这个值用在data-streaming中
            loadTestReportResultMapper.insertSelective(reportResult);
            // 启动测试
            engine.start();
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
            loadTestDTO.setIsNeedUpdate(apiPerformanceService.isNeedUpdate(loadTestDTO.getId()));
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
        String copyName = copy.getName() + " Copy";

        if (StringUtils.length(copyName) > 30) {
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
            performanceReportService.deleteReport(reportId);
        } else {
            stopEngine(reportId);
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

    public List<ScheduleDao> listSchedule(QueryScheduleRequest request) {
        request.setEnable(true);
        List<ScheduleDao> schedules = scheduleService.list(request);
        List<String> resourceIds = schedules.stream()
                .map(Schedule::getResourceId)
                .collect(Collectors.toList());
        if (!resourceIds.isEmpty()) {
            LoadTestExample example = new LoadTestExample();
            LoadTestExample.Criteria criteria = example.createCriteria();
            if (StringUtils.isNotBlank(request.getProjectId())) {
                criteria.andProjectIdEqualTo(request.getProjectId());
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
            List<String> fileTypes = Arrays.stream(FileType.values())
                    .filter(fileType -> !fileType.equals(FileType.JMX))
                    .map(FileType::name)
                    .collect(Collectors.toList());
            loadTypes.addAll(fileTypes);
        }
        if (StringUtils.equalsIgnoreCase(loadType, "all")) {
            List<String> fileTypes = Arrays.stream(FileType.values())
                    .map(FileType::name)
                    .collect(Collectors.toList());
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
            FileMetadata fileMetadata = fileService.getFileMetadataById(id);
            FileContent fileContent = fileService.getFileContent(id);
            results.add(new LoadTestExportJmx(fileMetadata.getName(), new String(fileContent.getFile(), StandardCharsets.UTF_8)));
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

    public String deleteBatchLog(DeletePerformanceRequest request) {
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> getLoadTestIds(request.getProjectId()));
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

    /**
     * 一键更新由接口用例或者场景用例转换的性能测试
     *
     * @param request
     */
    public void syncApi(EditTestPlanRequest request) {
        String lostTestId = request.getId();
        LoadTestWithBLOBs loadTest = loadTestMapper.selectByPrimaryKey(lostTestId);
        if (loadTest == null) {
            MSException.throwException(Translator.get("edit_load_test_not_found") + lostTestId);
        }
        if (StringUtils.containsAny(loadTest.getStatus(), PerformanceTestStatus.Running.name(), PerformanceTestStatus.Starting.name())) {
            MSException.throwException(Translator.get("cannot_edit_load_test_running"));
        }
        List<ApiLoadTest> apiLoadTests = apiPerformanceService.getByLoadTestId(loadTest.getId());
        syncScenario(loadTest, apiLoadTests);
        syncApiCase(loadTest, apiLoadTests);
    }

    public void syncScenario(LoadTestWithBLOBs loadTest, List<ApiLoadTest> apiLoadTests) {
        List<String> scenarioIds = apiLoadTests.stream()
                .filter(i -> i.getType().equals(ApiLoadType.SCENARIO.name()))
                .map(ApiLoadTest::getApiId)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(scenarioIds)) {
            ApiScenarioBatchRequest scenarioRequest = new ApiScenarioBatchRequest();
            scenarioRequest.setIds(scenarioIds);
            List<ApiScenarioExportJmxDTO> apiScenrioExportJmxes = apiAutomationService.exportJmx(scenarioRequest);

            deleteLoadTestFiles(loadTest.getId());

            apiScenrioExportJmxes.forEach(item -> {
                apiPerformanceService.UpdateVersion(loadTest.getId(), item.getId(), item.getVersion());
                saveJmxFile(item.getJmx(), item.getName(), loadTest.getProjectId(), loadTest.getId());
                saveOtherFile(item.getFileMetadataList(), loadTest.getId());
            });
        }
    }

    public void syncApiCase(LoadTestWithBLOBs loadTest, List<ApiLoadTest> apiLoadTests) {
        List<String> caseIds = apiLoadTests.stream()
                .filter(i -> i.getType().equals(ApiLoadType.API_CASE.name()))
                .map(ApiLoadTest::getApiId)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(caseIds)) {
            ApiScenarioBatchRequest scenarioRequest = new ApiScenarioBatchRequest();
            scenarioRequest.setIds(caseIds);

            List<JmxInfoDTO> jmxInfoDTOS = apiTestCaseService.exportJmx(caseIds, apiLoadTests.get(0).getEnvId());
            deleteLoadTestFiles(loadTest.getId());
            jmxInfoDTOS.forEach(item -> {
                apiPerformanceService.UpdateVersion(loadTest.getId(), item.getId(), item.getVersion());
                saveJmxFile(item.getXml(), item.getName(), loadTest.getProjectId(), loadTest.getId());
                saveBodyFile(item.getFileMetadataList(), loadTest.getId(), item.getId());
            });
        }
    }

    private void saveJmxFile(String jmx, String name, String projectId, String loadTestId) {
        byte[] jmxBytes = jmx.getBytes(StandardCharsets.UTF_8);
        String jmxName = name + "_" + System.currentTimeMillis() + ".jmx";
        FileMetadata fileMetadata = fileService.saveFile(jmxBytes, jmxName, (long) jmxBytes.length);
        fileMetadata.setProjectId(projectId);
        saveLoadTestFile(fileMetadata, loadTestId, 0);
    }

    private void saveOtherFile(List<FileMetadata> fileNames, String loadTestId) {
        for (int i = 0; i < fileNames.size(); i++) {
            FileMetadata model = fileNames.get(i);
            String fileName = model.getName();
            File file = FileUtils.getFileByName(fileName);
            saveUploadFile(file, loadTestId, i + 1);
        }
    }

    private void saveBodyFile(List<FileMetadata> fileNames, String loadTestId, String requestId) {
        for (int i = 0; i < fileNames.size(); i++) {
            String fileName = fileNames.get(i).getName();
            File file = FileUtils.getBodyFileByName(fileName, requestId);
            saveUploadFile(file, loadTestId, i + 1);
        }
    }

    private void deleteLoadTestFiles(String testId) {
        List<FileMetadata> originFiles = getFileMetadataByTestId(testId);
        List<String> originFileIds = originFiles.stream().map(FileMetadata::getId).collect(Collectors.toList());
        LoadTestFileExample example = new LoadTestFileExample();
        example.createCriteria()
                .andTestIdEqualTo(testId);
        loadTestFileMapper.deleteByExample(example);
        fileService.deleteFileByIds(originFileIds);
    }

    private void saveUploadFile(File file, String loadTestId, int sort) {
        if (file != null) {
            FileMetadata fileMetadata = null;
            try {
                fileMetadata = fileService.saveFile(file, FileUtil.readAsByteArray(file));
            } catch (IOException e) {
                LogUtil.error(e);
            }
            saveLoadTestFile(fileMetadata, loadTestId, sort);
        }
    }

    private void saveLoadTestFile(FileMetadata fileMetadata, String loadTestId, int sort) {
        if (fileMetadata != null) {
            LoadTestFile loadTestFile = new LoadTestFile();
            loadTestFile.setTestId(loadTestId);
            loadTestFile.setFileId(fileMetadata.getId());
            loadTestFile.setSort(sort);
            loadTestFileMapper.insert(loadTestFile);
        }
    }

    /**
     * 初始化场景与性能测试的关联关系
     */
    public void initScenarioLoadTest() {
        LoadTestExample example = new LoadTestExample();
        example.createCriteria().andScenarioIdIsNotNull();
        List<LoadTest> loadTests = loadTestMapper.selectByExample(example);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiLoadTestMapper mapper = sqlSession.getMapper(ApiLoadTestMapper.class);
        loadTests.forEach(item -> {
            ApiLoadTest scenarioLoadTest = new ApiLoadTest();
            scenarioLoadTest.setType(ApiLoadType.SCENARIO.name());
            scenarioLoadTest.setApiId(item.getScenarioId());
            scenarioLoadTest.setApiVersion(item.getScenarioVersion() == null ? 0 : item.getScenarioVersion());
            scenarioLoadTest.setLoadTestId(item.getId());
            scenarioLoadTest.setId(UUID.randomUUID().toString());
            mapper.insert(scenarioLoadTest);
        });
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
    }

    public Integer getGranularity(String reportId) {
        Integer granularity = CommonBeanFactory.getBean(JmeterProperties.class).getReport().getGranularity();
        try {
            LoadTestReportWithBLOBs report = loadTestReportMapper.selectByPrimaryKey(reportId);
            JSONObject advancedConfig = JSON.parseObject(report.getAdvancedConfiguration());
            if (advancedConfig.getInteger("granularity") != null) {
                return advancedConfig.getInteger("granularity") * 1000;// 单位是ms
            }
            AtomicReference<Integer> maxDuration = new AtomicReference<>(0);
            List<List<JSONObject>> pressureConfigLists = JSON.parseObject(report.getLoadConfiguration(), new TypeReference<List<List<JSONObject>>>() {
            });
            // 按照最长的执行时间来确定
            pressureConfigLists.forEach(pcList -> {

                Optional<Integer> maxOp = pcList.stream()
                        .filter(pressureConfig -> StringUtils.equalsAnyIgnoreCase(pressureConfig.getString("key"), "hold", "duration"))
                        .map(pressureConfig -> pressureConfig.getInteger("value"))
                        .max(Comparator.naturalOrder());
                Integer max = maxOp.orElse(0);
                if (maxDuration.get() < max) {
                    maxDuration.set(max);
                }
            });
            Optional<GranularityData.Data> dataOptional = GranularityData.dataList.stream()
                    .filter(data -> maxDuration.get() >= data.getStart() && maxDuration.get() <= data.getEnd())
                    .findFirst();

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
            example.createCriteria().andIdIn(ids);
            return loadTestMapper.selectByExample(example);
        }
        return new ArrayList<>();
    }

    public void initOrderField() {
        ServiceUtils.initOrderField(LoadTestWithBLOBs.class, LoadTestMapper.class,
                extLoadTestMapper::selectProjectIds,
                extLoadTestMapper::getIdsOrderByUpdateTime);
    }

    /**
     * 用例自定义排序
     *
     * @param request
     */
    public void updateOrder(ResetOrderRequest request) {
        ServiceUtils.updateOrderField(request, LoadTestWithBLOBs.class,
                loadTestMapper::selectByPrimaryKey,
                extLoadTestMapper::getPreOrder,
                extLoadTestMapper::getLastOrder,
                loadTestMapper::updateByPrimaryKeySelective);
    }

    public List<LoadTestReportWithBLOBs> selectReportsByTestResourcePoolId(String resourcePoolId) {
        LoadTestReportExample example = new LoadTestReportExample();
        example.createCriteria().andTestResourcePoolIdEqualTo(resourcePoolId)
                .andStatusIn(Arrays.asList(PerformanceTestStatus.Running.name(), PerformanceTestStatus.Starting.name()));
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

    public List<LoadModuleDTO> getNodeByPlanId(String planId) {
        List<LoadModuleDTO> list = new ArrayList<>();
        List<String> projectIds = testPlanProjectService.getProjectIdsByPlanId(planId);
        projectIds.forEach(id -> {
            Project project = projectMapper.selectByPrimaryKey(id);
            String name = project.getName();
            LoadModuleDTO loadModuleDTO = new LoadModuleDTO();
            loadModuleDTO.setId(id);
            loadModuleDTO.setName(name);
            loadModuleDTO.setLabel(name);
            LoadCaseRequest request = new LoadCaseRequest();
            request.setProjectId(id);
            request.setTestPlanId(planId);
            List<String> ids = testPlanLoadCaseService.selectTestPlanLoadCaseIds(request);
            if (!CollectionUtils.isEmpty(ids)) {
                list.add(loadModuleDTO);
            }
        });
        return list;
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
        ServiceUtils.getSelectAllIds(request, request.getCondition(),
                (query) -> getLoadTestIds(request.getProjectId()));
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


}
