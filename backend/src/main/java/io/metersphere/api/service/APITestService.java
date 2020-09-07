package io.metersphere.api.service;

import com.alibaba.fastjson.JSONObject;
import io.github.ningyu.jmeter.plugin.dubbo.sample.ProviderService;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.parse.ApiImport;
import io.metersphere.api.dto.scenario.request.dubbo.RegistryCenter;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.parse.ApiImportParser;
import io.metersphere.api.parse.ApiImportParserFactory;
import io.metersphere.api.parse.JmeterDocumentParser;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiTestFileMapper;
import io.metersphere.base.mapper.ApiTestMapper;
import io.metersphere.base.mapper.ext.ExtApiTestMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.FileType;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.constants.ScheduleType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.QueryScheduleRequest;
import io.metersphere.dto.ScheduleDao;
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.ApiTestJob;
import io.metersphere.service.FileService;
import io.metersphere.service.QuotaService;
import io.metersphere.service.ScheduleService;
import io.metersphere.track.service.TestCaseService;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.aspectj.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class APITestService {

    @Resource
    private ApiTestMapper apiTestMapper;
    @Resource
    private ExtApiTestMapper extApiTestMapper;
    @Resource
    private ApiTestFileMapper apiTestFileMapper;
    @Resource
    private FileService fileService;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private APIReportService apiReportService;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private TestCaseService testCaseService;

    private static final String BODY_FILE_DIR = "/opt/metersphere/data/body";

    public List<APITestResult> list(QueryAPITestRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extApiTestMapper.list(request);
    }

    public List<APITestResult> recentTest(QueryAPITestRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        return extApiTestMapper.list(request);
    }

    public List<ApiTest> listByIds(QueryAPITestRequest request) {
        return extApiTestMapper.listByIds(request.getIds());
    }

    public void create(SaveAPITestRequest request, MultipartFile file, List<MultipartFile> bodyFiles) {
        if (file == null) {
            throw new IllegalArgumentException(Translator.get("file_cannot_be_null"));
        }
        checkQuota();
        List<String> bodyUploadIds = new ArrayList<>(request.getBodyUploadIds());
        request.setBodyUploadIds(null);
        ApiTest test = createTest(request);
        createBodyFiles(test, bodyUploadIds, bodyFiles);
        saveFile(test.getId(), file);
    }

    public void update(SaveAPITestRequest request, MultipartFile file, List<MultipartFile> bodyFiles) {
        if (file == null) {
            throw new IllegalArgumentException(Translator.get("file_cannot_be_null"));
        }
        deleteFileByTestId(request.getId());

        List<String> bodyUploadIds = new ArrayList<>(request.getBodyUploadIds());
        request.setBodyUploadIds(null);
        ApiTest test = updateTest(request);
        createBodyFiles(test, bodyUploadIds, bodyFiles);
        saveFile(test.getId(), file);
    }

    private void createBodyFiles(ApiTest test, List<String> bodyUploadIds, List<MultipartFile> bodyFiles) {
        if (bodyFiles == null || bodyFiles.isEmpty()) {

        }
        String dir = BODY_FILE_DIR + "/" + test.getId();
        File testDir = new File(dir);
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        for (int i = 0; i < bodyUploadIds.size(); i++) {
            MultipartFile item = bodyFiles.get(i);
            File file = new File(testDir + "/" + bodyUploadIds.get(i) + "_" + item.getOriginalFilename());
            InputStream in = null;
            OutputStream out = null;
            try {
                file.createNewFile();
                in = item.getInputStream();
                out = new FileOutputStream(file);
                FileUtil.copyStream(in, out);
            } catch (IOException e) {
                LogUtil.error(e);
                MSException.throwException(Translator.get("upload_fail"));
            } finally {
                try {
                    in.close();
                    out.close();
                } catch (IOException e) {
                    LogUtil.error(e);
                    MSException.throwException(Translator.get("upload_fail"));
                }
            }
        }
    }

    public void copy(SaveAPITestRequest request) {
        checkQuota();

        ApiTestExample example = new ApiTestExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId());
        if (apiTestMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("load_test_already_exists"));
        }

        // copy test
        ApiTest copy = get(request.getId());
        copy.setId(UUID.randomUUID().toString());
        copy.setName(request.getName());
        copy.setCreateTime(System.currentTimeMillis());
        copy.setUpdateTime(System.currentTimeMillis());
        copy.setStatus(APITestStatus.Saved.name());
        copy.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        apiTestMapper.insert(copy);
        // copy test file
        ApiTestFile apiTestFile = getFileByTestId(request.getId());
        if (apiTestFile != null) {
            FileMetadata fileMetadata = fileService.copyFile(apiTestFile.getFileId());
            apiTestFile.setTestId(copy.getId());
            apiTestFile.setFileId(fileMetadata.getId());
            apiTestFileMapper.insert(apiTestFile);
        }
        copyBodyFiles(copy.getId(), request.getId());
    }

    public void copyBodyFiles(String target, String source) {
        String sourceDir = BODY_FILE_DIR + "/" + source;
        String targetDir = BODY_FILE_DIR + "/" + target;
        File sourceFile = new File(sourceDir);
        if (sourceFile.exists()) {
            try {
                FileUtil.copyDir(sourceFile, new File(targetDir));
            } catch (IOException e) {
                LogUtil.error(e);
                MSException.throwException(Translator.get("upload_fail"));
            }
        }
    }

    public APITestResult get(String id) {
        APITestResult apiTest = new APITestResult();
        ApiTest test = apiTestMapper.selectByPrimaryKey(id);
        if (test != null) {
            BeanUtils.copyBean(apiTest, test);
            Schedule schedule = scheduleService.getScheduleByResource(id, ScheduleGroup.API_TEST.name());
            apiTest.setSchedule(schedule);
            return apiTest;
        }
        return null;
    }


    public List<ApiTest> getApiTestByProjectId(String projectId) {
        return extApiTestMapper.getApiTestByProjectId(projectId);
    }

    public void delete(String testId) {
        testCaseService.checkIsRelateTest(testId);
        deleteFileByTestId(testId);
        apiReportService.deleteByTestId(testId);
        scheduleService.deleteByResourceId(testId);
        apiTestMapper.deleteByPrimaryKey(testId);
        deleteBodyFiles(testId);
    }

    public void deleteBodyFiles(String testId) {
        File file = new File(BODY_FILE_DIR + "/" + testId);
        FileUtil.deleteContents(file);
        if (file.exists()) {
            file.delete();
        }
    }

    public String run(SaveAPITestRequest request) {
        ApiTestFile file = getFileByTestId(request.getId());
        if (file == null) {
            MSException.throwException(Translator.get("file_cannot_be_null"));
        }
        byte[] bytes = fileService.loadFileAsBytes(file.getFileId());
        // 解析 xml 处理 mock 数据
        bytes = JmeterDocumentParser.parse(bytes);
        InputStream is = new ByteArrayInputStream(bytes);

        APITestResult apiTest = get(request.getId());
        if (SessionUtils.getUser() == null) {
            apiTest.setUserId(request.getUserId());
        }
        String reportId = apiReportService.create(apiTest, request.getTriggerMode());
        changeStatus(request.getId(), APITestStatus.Running);

        jMeterService.run(request.getId(), null, is);
        return reportId;
    }

    public void changeStatus(String id, APITestStatus status) {
        ApiTest apiTest = new ApiTest();
        apiTest.setId(id);
        apiTest.setStatus(status.name());
        apiTestMapper.updateByPrimaryKeySelective(apiTest);
    }

    private void checkNameExist(SaveAPITestRequest request) {
        ApiTestExample example = new ApiTestExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId()).andIdNotEqualTo(request.getId());
        if (apiTestMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("load_test_already_exists"));
        }
    }

    public void checkName(SaveAPITestRequest request) {
        ApiTestExample example = new ApiTestExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId());
        if (apiTestMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("load_test_already_exists"));
        }
    }

    private ApiTest updateTest(SaveAPITestRequest request) {
        checkNameExist(request);
        final ApiTest test = new ApiTest();
        test.setId(request.getId());
        test.setName(request.getName());
        test.setProjectId(request.getProjectId());
        test.setScenarioDefinition(JSONObject.toJSONString(request.getScenarioDefinition()));
        test.setUpdateTime(System.currentTimeMillis());
        test.setStatus(APITestStatus.Saved.name());
        apiTestMapper.updateByPrimaryKeySelective(test);
        return test;
    }

    private ApiTest createTest(SaveAPITestRequest request) {
        checkNameExist(request);
        final ApiTest test = new ApiTest();
        test.setId(request.getId());
        test.setName(request.getName());
        test.setProjectId(request.getProjectId());
        test.setScenarioDefinition(JSONObject.toJSONString(request.getScenarioDefinition()));
        test.setCreateTime(System.currentTimeMillis());
        test.setUpdateTime(System.currentTimeMillis());
        test.setStatus(APITestStatus.Saved.name());
        test.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        apiTestMapper.insert(test);
        return test;
    }

    private void saveFile(String testId, MultipartFile file) {
        final FileMetadata fileMetadata = fileService.saveFile(file);
        ApiTestFile apiTestFile = new ApiTestFile();
        apiTestFile.setTestId(testId);
        apiTestFile.setFileId(fileMetadata.getId());
        apiTestFileMapper.insert(apiTestFile);
    }

    private void deleteFileByTestId(String testId) {
        ApiTestFileExample ApiTestFileExample = new ApiTestFileExample();
        ApiTestFileExample.createCriteria().andTestIdEqualTo(testId);
        final List<ApiTestFile> ApiTestFiles = apiTestFileMapper.selectByExample(ApiTestFileExample);
        apiTestFileMapper.deleteByExample(ApiTestFileExample);

        if (!CollectionUtils.isEmpty(ApiTestFiles)) {
            final List<String> fileIds = ApiTestFiles.stream().map(ApiTestFile::getFileId).collect(Collectors.toList());
            fileService.deleteFileByIds(fileIds);
        }
    }

    private ApiTestFile getFileByTestId(String testId) {
        ApiTestFileExample ApiTestFileExample = new ApiTestFileExample();
        ApiTestFileExample.createCriteria().andTestIdEqualTo(testId);
        final List<ApiTestFile> ApiTestFiles = apiTestFileMapper.selectByExample(ApiTestFileExample);
        apiTestFileMapper.selectByExample(ApiTestFileExample);
        if (!CollectionUtils.isEmpty(ApiTestFiles)) {
            return ApiTestFiles.get(0);
        } else {
            return null;
        }
    }

    public void updateSchedule(Schedule request) {
        scheduleService.editSchedule(request);
        addOrUpdateApiTestCronJob(request);
    }

    public void createSchedule(Schedule request) {
        scheduleService.addSchedule(buildApiTestSchedule(request));
        addOrUpdateApiTestCronJob(request);
    }

    private Schedule buildApiTestSchedule(Schedule request) {
        Schedule schedule = scheduleService.buildApiTestSchedule(request);
        schedule.setJob(ApiTestJob.class.getName());
        schedule.setGroup(ScheduleGroup.API_TEST.name());
        schedule.setType(ScheduleType.CRON.name());
        return schedule;
    }

    private void addOrUpdateApiTestCronJob(Schedule request) {
        scheduleService.addOrUpdateCronJob(request, ApiTestJob.getJobKey(request.getResourceId()), ApiTestJob.getTriggerKey(request.getResourceId()), ApiTestJob.class);
    }

    public ApiTest apiTestImport(MultipartFile file, ApiTestImportRequest request) {
        ApiImportParser apiImportParser = ApiImportParserFactory.getApiImportParser(request.getPlatform());
        ApiImport apiImport = null;
        try {
            apiImport = Objects.requireNonNull(apiImportParser).parse(file == null ? null : file.getInputStream(), request);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("parse_data_error"));
        }
        SaveAPITestRequest saveRequest = getImportApiTest(request, apiImport);
        return createTest(saveRequest);
    }

    private SaveAPITestRequest getImportApiTest(ApiTestImportRequest importRequest, ApiImport apiImport) {
        SaveAPITestRequest request = new SaveAPITestRequest();
        request.setName(importRequest.getName());
        request.setProjectId(importRequest.getProjectId());
        request.setScenarioDefinition(apiImport.getScenarios());
        request.setUserId(SessionUtils.getUserId());
        request.setId(UUID.randomUUID().toString());
        for (FileType fileType : FileType.values()) {
            String suffix = fileType.suffix();
            String name = request.getName();
            if (name.endsWith(suffix)) {
                request.setName(name.substring(0, name.length() - suffix.length()));
            }
        }
        return request;
    }

    public List<DubboProvider> getProviders(RegistryCenter registry) {
        ProviderService providerService = ProviderService.get(registry.getAddress());
        List<String> providers = providerService.getProviders(registry.getProtocol(), registry.getAddress(), registry.getGroup());
        List<DubboProvider> list = new ArrayList<>();
        providers.forEach(p -> {
            DubboProvider provider = new DubboProvider();
            String[] info = p.split(":");
            if (info.length > 1) {
                provider.setVersion(info[1]);
            }
            provider.setService(p);
            provider.setServiceInterface(info[0]);
            Map<String, URL> services = providerService.findByService(p);
            if (services != null && !services.isEmpty()) {
                String[] methods = services.values().stream().findFirst().get().getParameter(CommonConstants.METHODS_KEY).split(",");
                provider.setMethods(Arrays.asList(methods));
            } else {
                provider.setMethods(new ArrayList<>());
            }
            list.add(provider);
        });
        return list;
    }

    public List<ScheduleDao> listSchedule(QueryScheduleRequest request) {
        request.setEnable(true);
        List<ScheduleDao> schedules = scheduleService.list(request);
        List<String> resourceIds = schedules.stream()
                .map(Schedule::getResourceId)
                .collect(Collectors.toList());
        if (!resourceIds.isEmpty()) {
            ApiTestExample example = new ApiTestExample();
            example.createCriteria().andIdIn(resourceIds);
            List<ApiTest> apiTests = apiTestMapper.selectByExample(example);
            Map<String, String> apiTestMap = apiTests.stream().collect(Collectors.toMap(ApiTest::getId, ApiTest::getName));
            scheduleService.build(apiTestMap, schedules);
        }
        return schedules;
    }

    public String runDebug(SaveAPITestRequest request, MultipartFile file, List<MultipartFile> bodyFiles) {
        if (file == null) {
            throw new IllegalArgumentException(Translator.get("file_cannot_be_null"));
        }
        updateTest(request);
        APITestResult apiTest = get(request.getId());
        List<String> bodyUploadIds = new ArrayList<>(request.getBodyUploadIds());
        request.setBodyUploadIds(null);
        createBodyFiles(apiTest, bodyUploadIds, bodyFiles);
        if (SessionUtils.getUser() == null) {
            apiTest.setUserId(request.getUserId());
        }
        String reportId = apiReportService.createDebugReport(apiTest);

        InputStream is = null;
        try {
            byte[] bytes = file.getBytes();
            // 解析 xml 处理 mock 数据
            bytes = JmeterDocumentParser.parse(bytes);
            is = new ByteArrayInputStream(bytes);
        } catch (IOException e) {
            LogUtil.error(e);
        }

        jMeterService.run(request.getId(), reportId, is);
        return reportId;
    }

    private void checkQuota() {
        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        if (quotaService != null) {
            quotaService.checkAPITestQuota();
        }
    }
}
