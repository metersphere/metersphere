package io.metersphere.api.service;

import com.alibaba.fastjson.JSONObject;
import io.github.ningyu.jmeter.plugin.dubbo.sample.ProviderService;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.parse.ApiImport;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.dto.scenario.request.dubbo.RegistryCenter;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.parse.old.ApiImportParser;
import io.metersphere.api.parse.old.ApiImportParserFactory;
import io.metersphere.api.parse.old.JmeterDocumentParser;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiTestFileMapper;
import io.metersphere.base.mapper.ApiTestMapper;
import io.metersphere.base.mapper.ext.ExtApiTestMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.FileType;
import io.metersphere.commons.constants.ScheduleGroup;
import io.metersphere.commons.constants.ScheduleType;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.controller.request.QueryScheduleRequest;
import io.metersphere.controller.request.ScheduleRequest;
import io.metersphere.dto.ScheduleDao;
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.ApiTestJob;
import io.metersphere.performance.parse.EngineSourceParserFactory;
import io.metersphere.service.FileService;
import io.metersphere.service.ScheduleService;
import io.metersphere.track.service.TestCaseService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.jorphan.collections.HashTree;
import org.aspectj.util.FileUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class APITestService {
    @Resource
    private ApiTestEnvironmentService apiTestEnvironmentService;
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
        List<String> bodyUploadIds = new ArrayList<>(request.getBodyUploadIds());
        ApiTest test = createTest(request, file);
        createBodyFiles(test, bodyUploadIds, bodyFiles);
    }

    private ApiTest createTest(SaveAPITestRequest request, MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException(Translator.get("file_cannot_be_null"));
        }
        request.setBodyUploadIds(null);
        ApiTest test = createTest(request);
        saveFile(test, file);
        return test;
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
        saveFile(test, file);
    }

    private void createBodyFiles(ApiTest test, List<String> bodyUploadIds, List<MultipartFile> bodyFiles) {
        if (bodyUploadIds.size() <= 0) {
            return;
        }
        String dir = BODY_FILE_DIR + "/" + test.getId();
        File testDir = new File(dir);
        if (!testDir.exists()) {
            testDir.mkdirs();
        }
        for (int i = 0; i < bodyUploadIds.size(); i++) {
            MultipartFile item = bodyFiles.get(i);
            File file = new File(testDir + "/" + bodyUploadIds.get(i) + "_" + item.getOriginalFilename());
            try (InputStream in = item.getInputStream(); OutputStream out = new FileOutputStream(file)) {
                file.createNewFile();
                FileUtil.copyStream(in, out);
            } catch (IOException e) {
                LogUtil.error(e.getMessage(), e);
                MSException.throwException(Translator.get("upload_fail"));
            }
        }
    }

    public void copy(SaveAPITestRequest request) {

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
                LogUtil.error(e.getMessage(), e);
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

    public void delete(DeleteAPITestRequest request) {
        String testId = request.getId();
        if (!request.isForceDelete()) {
            testCaseService.checkIsRelateTest(testId);
        }
        deleteFileByTestId(testId);
        apiReportService.deleteByTestId(testId);
        scheduleService.deleteByResourceId(testId, ScheduleGroup.API_TEST.name());
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

    private void saveFile(ApiTest apiTest, MultipartFile file) {
        final FileMetadata fileMetadata = fileService.saveFile(file, apiTest.getProjectId());
        ApiTestFile apiTestFile = new ApiTestFile();
        apiTestFile.setTestId(apiTest.getId());
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

    public void createSchedule(ScheduleRequest request) {
        scheduleService.addSchedule(buildApiTestSchedule(request));
        addOrUpdateApiTestCronJob(request);
    }

    private Schedule buildApiTestSchedule(ScheduleRequest request) {
        Schedule schedule = scheduleService.buildApiTestSchedule(request);
        schedule.setJob(ApiTestJob.class.getName());
        schedule.setGroup(ScheduleGroup.API_TEST.name());
        schedule.setType(ScheduleType.CRON.name());
        schedule.setProjectId(request.getProjectId());
        schedule.setName(request.getName());
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
            ApiTestExample.Criteria criteria = example.createCriteria();
            criteria.andIdIn(resourceIds);
            if (StringUtils.isNotBlank(request.getProjectId())) {
                criteria.andProjectIdEqualTo(request.getProjectId());
            }
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
            LogUtil.error(e.getMessage(), e);
        }
        return reportId;
    }

    public void mergeCreate(SaveAPITestRequest request, MultipartFile file, List<String> selectIds) {
        ApiTest test = createTest(request, file);
        selectIds.forEach(sourceId -> {
            copyBodyFiles(test.getId(), sourceId);
        });
    }

    /**
     * 更新jmx数据，处理jmx里的各种参数
     * <p>
     *
     * @param jmx 原JMX文件
     * @return
     * @author song tianyang
     */
    public JmxInfoDTO updateJmxString(String jmx, String projectId) {
        jmx = this.updateJmxMessage(jmx);

        //获取要转化的文件
        List<String> attachmentFilePathList = new ArrayList<>();
        try {
            Document doc = EngineSourceParserFactory.getDocument(new ByteArrayInputStream(jmx.getBytes("utf-8")));
            Element root = doc.getRootElement();
            Element rootHashTreeElement = root.element("hashTree");
            List<Element> innerHashTreeElementList = rootHashTreeElement.elements("hashTree");
            for (Element innerHashTreeElement : innerHashTreeElementList) {
                List<Element> thirdHashTreeElementList = innerHashTreeElement.elements();
                for (Element element : thirdHashTreeElementList) {
                    //HTTPSamplerProxy， 进行附件转化： 1.elementProp里去掉路径； 2。elementProp->filePath获取路径并读出来
                    attachmentFilePathList.addAll(this.parseAttachmentFileInfo(element));
                }
                //如果存在证书文件，也要匹配出来
                attachmentFilePathList.addAll(this.parseAttachmentFileInfo(rootHashTreeElement));
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        if (!jmx.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
            jmx = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + jmx;
        }
        //处理附件
        Map<String, String> attachmentFiles = new HashMap<>();
        //去重处理
        if (!CollectionUtils.isEmpty(attachmentFilePathList)) {
            attachmentFilePathList = attachmentFilePathList.stream().distinct().collect(Collectors.toList());
        }
        List<FileMetadata> fileMetadataList = new ArrayList<>();
        for (String filePath : attachmentFilePathList) {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                try {
                    FileMetadata fileMetadata = fileService.insertFileByFileName(file, FileUtil.readAsByteArray(file), projectId);
                    if (fileMetadata != null) {
                        fileMetadataList.add(fileMetadata);
                        attachmentFiles.put(fileMetadata.getId(), fileMetadata.getName());
                    }
                } catch (Exception e) {
                    LogUtil.error(e);
                }
            }
        }

        JmxInfoDTO returnDTO = new JmxInfoDTO("Demo.jmx", jmx, attachmentFiles);
        returnDTO.setFileMetadataList(fileMetadataList);
        return returnDTO;
    }

    private void checkAndRemoveRunningDebugSampler(Element element) {
        List<Element> childElements = element.elements();
        if (CollectionUtils.isNotEmpty(childElements)) {
            if (childElements.size() > 1) {
                Element checkElement = childElements.get(childElements.size() - 2);
                String elementName = checkElement.attributeValue("testname");
                if (StringUtils.equalsIgnoreCase(elementName, RunningParamKeys.RUNNING_DEBUG_SAMPLER_NAME)) {
                    Element checkHashTreeElement = childElements.get(childElements.size() - 1);
                    if (StringUtils.equalsIgnoreCase("hashtree", checkHashTreeElement.getName())) {
                        element.remove(checkHashTreeElement);
                        element.remove(checkElement);
                    }
                }
            }
        }
    }

    private List<String> parseAttachmentFileInfo(Element parentHashTreeElement) {
        List<String> attachmentFilePathList = new ArrayList<>();
        List<Element> parentElementList = parentHashTreeElement.elements();
        for (Element parentElement : parentElementList) {
            String qname = parentElement.getQName().getName();
            if (StringUtils.equals(qname, "CSVDataSet")) {
                try {
                    List<Element> propElementList = parentElement.elements();
                    for (Element propElement : propElementList) {
                        if (StringUtils.equals("filename", propElement.attributeValue("name"))) {
                            String filePath = propElement.getText();
                            File file = new File(filePath);
                            if (file.exists() && file.isFile()) {
                                attachmentFilePathList.add(filePath);
                                String fileName = file.getName();
                            }
                        }
                    }
                } catch (Exception e) {
                }
            } else if (StringUtils.equals(qname, "HTTPSamplerProxy")) {
                List<Element> elementPropElementList = parentElement.elements("elementProp");
                for (Element element : elementPropElementList) {
                    if (StringUtils.equals(element.attributeValue("name"), "HTTPsampler.Files")) {
                        String name = element.getName();
                        List<Element> collectionPropList = element.elements("collectionProp");
                        for (Element prop : collectionPropList) {
                            List<Element> elementProps = prop.elements();
                            for (Element elementProp : elementProps) {
                                if (StringUtils.equals(elementProp.attributeValue("elementType"), "HTTPFileArg")) {
                                    try {
                                        String filePath = elementProp.attributeValue("name");
                                        File file = new File(filePath);
                                        if (file.exists() && file.isFile()) {
                                            attachmentFilePathList.add(filePath);
                                            String fileName = file.getName();
                                            elementProp.attribute("name").setText(fileName);
                                        }
                                    } catch (Exception e) {
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (StringUtils.equals(qname, "KeystoreConfig")) {
                List<Element> stringPropList = parentElement.elements("stringProp");
                for (Element element : stringPropList) {
                    if (StringUtils.equals(element.attributeValue("name"), "MS-KEYSTORE-FILE-PATH")) {

                        try {
                            String filePath = element.getStringValue();
                            File file = new File(filePath);
                            if (file.exists() && file.isFile()) {
                                attachmentFilePathList.add(filePath);
                                String fileName = file.getName();
                                element.setText(fileName);
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            } else if (StringUtils.equals(qname, "hashTree")) {
                attachmentFilePathList.addAll(this.parseAttachmentFileInfo(parentElement));
            }
        }

        return attachmentFilePathList;
    }

    private String updateJmxMessage(String jmx) {
        if (StringUtils.isNotEmpty(jmx)) {
            jmx = StringUtils.replace(jmx, "<DubboSample", "<io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample");
            jmx = StringUtils.replace(jmx, "</DubboSample>", "</io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample>");
            jmx = StringUtils.replace(jmx, " guiclass=\"DubboSampleGui\" ", " guiclass=\"io.github.ningyu.jmeter.plugin.dubbo.gui.DubboSampleGui\" ");
            jmx = StringUtils.replace(jmx, " guiclass=\"DubboDefaultConfigGui\" ", " guiclass=\"io.github.ningyu.jmeter.plugin.dubbo.gui.DubboDefaultConfigGui\" ");
            jmx = StringUtils.replace(jmx, " testclass=\"DubboSample\" ", " testclass=\"io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample\" ");
        }
        return jmx;
    }


    public JmxInfoDTO getJmxInfoDTO(RunDefinitionRequest runRequest, List<MultipartFile> bodyFiles) {
        ParameterConfig config = new ParameterConfig();
        config.setProjectId(runRequest.getProjectId());
        config.setOperating(true);

        Map<String, EnvironmentConfig> envConfig = new HashMap<>();
        Map<String, String> map = runRequest.getEnvironmentMap();
        if (map != null && map.size() > 0) {
            ApiTestEnvironmentWithBLOBs environment = apiTestEnvironmentService.get(map.get(runRequest.getProjectId()));
            EnvironmentConfig env = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
            envConfig.put(runRequest.getProjectId(), env);
            config.setConfig(envConfig);
        }
        HashTree hashTree = runRequest.getTestElement().generateHashTree(config);
        String jmxString = runRequest.getTestElement().getJmx(hashTree);
        //将jmx处理封装为通用方法
        JmxInfoDTO dto = updateJmxString(jmxString, runRequest.getProjectId());
        dto.setName(runRequest.getName() + ".jmx");
        return dto;
    }
}
