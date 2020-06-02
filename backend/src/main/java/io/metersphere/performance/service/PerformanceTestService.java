package io.metersphere.performance.service;

import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtLoadTestMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportDetailMapper;
import io.metersphere.base.mapper.ext.ExtLoadTestReportMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.PerformanceTestStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.OrderRequest;
import io.metersphere.dto.DashboardTestDTO;
import io.metersphere.dto.LoadTestDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.performance.engine.Engine;
import io.metersphere.performance.engine.EngineFactory;
import io.metersphere.service.FileService;
import io.metersphere.service.TestResourceService;
import io.metersphere.track.request.testplan.*;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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

    public List<LoadTestDTO> list(QueryTestPlanRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extLoadTestMapper.list(request);
    }

    public void delete(DeleteTestPlanRequest request) {
        String testId = request.getId();
        LoadTestReportExample loadTestReportExample = new LoadTestReportExample();
        loadTestReportExample.createCriteria().andTestIdEqualTo(testId);
        List<LoadTestReport> loadTestReports = loadTestReportMapper.selectByExample(loadTestReportExample);

        if (!loadTestReports.isEmpty()) {
            List<String> reportIdList = loadTestReports.stream().map(LoadTestReport::getId).collect(Collectors.toList());

            // delete load_test_report_result
            LoadTestReportResultExample loadTestReportResultExample = new LoadTestReportResultExample();
            loadTestReportResultExample.createCriteria().andReportIdIn(reportIdList);
            loadTestReportResultMapper.deleteByExample(loadTestReportResultExample);

            // delete load_test_report, delete load_test_report_detail
            reportIdList.forEach(reportId -> {
                LoadTestReportDetailExample example = new LoadTestReportDetailExample();
                example.createCriteria().andReportIdEqualTo(reportId);
                loadTestReportDetailMapper.deleteByExample(example);
                reportService.deleteReport(reportId);
            });
        }

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
        if (loadTest == null) {
            MSException.throwException(Translator.get("run_load_test_not_found") + request.getId());
        }

        if (StringUtils.equalsAny(loadTest.getStatus(), PerformanceTestStatus.Running.name(), PerformanceTestStatus.Starting.name())) {
            MSException.throwException(Translator.get("load_test_is_running"));
        }

        LogUtil.info("Load test started " + loadTest.getName());
        // engine type (NODE|K8S)
        final Engine engine = EngineFactory.createEngine(loadTest);
        if (engine == null) {
            MSException.throwException(String.format("Test cannot be run，test ID：%s", request.getId()));
        }

        startEngine(loadTest, engine);

        // todo：通过调用stop方法能够停止正在运行的engine，但是如果部署了多个backend实例，页面发送的停止请求如何定位到具体的engine

        return engine.getReportId();
    }

    private void startEngine(LoadTestWithBLOBs loadTest, Engine engine) {
        LoadTestReport testReport = new LoadTestReport();
        testReport.setId(engine.getReportId());
        testReport.setCreateTime(engine.getStartTime());
        testReport.setUpdateTime(engine.getStartTime());
        testReport.setTestId(loadTest.getId());
        testReport.setName(loadTest.getName());
        testReport.setUserId(SessionUtils.getUser().getId());
        // 启动测试

        try {
            engine.start();
            // 启动正常修改状态 starting
            loadTest.setStatus(PerformanceTestStatus.Starting.name());
            loadTestMapper.updateByPrimaryKeySelective(loadTest);
            // 启动正常插入 report
            testReport.setStatus(PerformanceTestStatus.Starting.name());
            loadTestReportMapper.insertSelective(testReport);

            LoadTestReportDetail reportDetail = new LoadTestReportDetail();
            reportDetail.setContent(HEADERS);
            reportDetail.setReportId(testReport.getId());
            reportDetail.setPart(1L);
            loadTestReportDetailMapper.insertSelective(reportDetail);
            // append \n
            extLoadTestReportDetailMapper.appendLine(testReport.getId(), "\n");
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
            return testDTOS.get(0);
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

    public void copy(SaveTestPlanRequest request) {
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
                loadTestFile.setTestId(copy.getId());
                loadTestFile.setFileId(fileMetadata.getId());
                loadTestFileMapper.insert(loadTestFile);
            });
        }
    }
}
