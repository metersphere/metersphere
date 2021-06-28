package io.metersphere.api.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.client.utils.StringUtils;
import io.github.ningyu.jmeter.plugin.dubbo.sample.ProviderService;
import io.metersphere.api.dto.*;
import io.metersphere.api.dto.parse.ApiImport;
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
import io.metersphere.commons.utils.*;
import io.metersphere.controller.request.QueryScheduleRequest;
import io.metersphere.controller.request.ScheduleRequest;
import io.metersphere.dto.ScheduleDao;
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.ApiTestJob;
import io.metersphere.service.FileService;
import io.metersphere.service.QuotaService;
import io.metersphere.service.ScheduleService;
import io.metersphere.service.UserService;
import io.metersphere.track.service.TestCaseService;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.aspectj.util.FileUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
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
    private UserService userService;
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
        checkQuota();
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
        /*if (request.getTriggerMode().equals("SCHEDULE")) {
            List<Notice> notice = noticeService.queryNotice(request.getId());
            mailService.sendHtml(reportId,notice,"api");
        }*/
        changeStatus(request.getId(), APITestStatus.Running);
        jMeterService.runOld(request.getId(), null, is);
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

        jMeterService.runOld(request.getId(), reportId, is);
        return reportId;
    }

    private void checkQuota() {
        QuotaService quotaService = CommonBeanFactory.getBean(QuotaService.class);
        if (quotaService != null) {
            quotaService.checkAPITestQuota();
        }
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
     * @param jmxString      原JMX文件
     * @param testNameParam  某些节点要替换的testName
     * @param isFromScenario 是否来源于场景 （来源于场景的话，testName要进行处理）
     * @return
     * @author song tianyang
     */
    public JmxInfoDTO updateJmxString(String jmxString, String testNameParam, boolean isFromScenario) {
        String attribute_testName = "testname";
        String[] requestElementNameArr = new String[]{"HTTPSamplerProxy", "TCPSampler", "JDBCSampler", "DubboSample"};

        List<String> attachmentFilePathList = new ArrayList<>();

        try {
            //将ThreadGroup的testname改为接口名称
            Document doc = DocumentHelper.parseText(jmxString);// 获取可续保保单列表报文模板
            Element root = doc.getRootElement();
            Element rootHashTreeElement = root.element("hashTree");

            List<Element> innerHashTreeElementList = rootHashTreeElement.elements("hashTree");
            for (Element innerHashTreeElement : innerHashTreeElementList) {
                //转换DubboDefaultConfigGui
                List<Element> configTestElementList = innerHashTreeElement.elements("ConfigTestElement");
                for (Element configTestElement : configTestElementList) {
                    this.updateDubboDefaultConfigGuiElement(configTestElement);
                }

                List<Element> theadGroupElementList = innerHashTreeElement.elements("ThreadGroup");
                for (Element theadGroupElement : theadGroupElementList) {
                    if (StringUtils.isNotEmpty(testNameParam)) {
                        theadGroupElement.attribute(attribute_testName).setText(testNameParam);
                    }
                }
                List<Element> thirdHashTreeElementList = innerHashTreeElement.elements("hashTree");
                for (Element element : thirdHashTreeElementList) {
                    String testName = testNameParam;

                    //更新请求类节点的部份属性
                    this.updateRequestElementInfo(element, testNameParam, requestElementNameArr, isFromScenario);
                    //检查有无jmeter不是别的自定义参数
                    this.checkPrivateFunctionNode(element);

                    //转换DubboDefaultConfigGui
                    List<Element> hashTreeConfigTestElementList = element.elements("ConfigTestElement");
                    for (Element configTestElement : hashTreeConfigTestElementList) {
                        this.updateDubboDefaultConfigGuiElement(configTestElement);
                    }

                    //HTTPSamplerProxy， 进行附件转化： 1.elementProp里去掉路径； 2。elementProp->filePath获取路径并读出来
                    attachmentFilePathList.addAll(this.parseAttachmentFileInfo(element));
                }
            }
            jmxString = root.asXML();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!jmxString.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")) {
            jmxString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + jmxString;
        }

        //处理附件
        Map<String, String> attachmentFiles = new HashMap<>();

        List<FileMetadata> fileMetadataList = new ArrayList<>();
        for (String filePath: attachmentFilePathList) {
            File file  = new File(filePath);
            if(file.exists() && file.isFile()){
                try{
                    FileMetadata fileMetadata = fileService.saveFile(file,FileUtil.readAsByteArray(file));
                    fileMetadataList.add(fileMetadata);
                    attachmentFiles.put(fileMetadata.getId(),fileMetadata.getName());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        JmxInfoDTO returnDTO = new JmxInfoDTO("Demo.jmx",jmxString,attachmentFiles);
        returnDTO.setFileMetadataList(fileMetadataList);
        return returnDTO;
    }

    private List<String> parseAttachmentFileInfo(Element parentHashTreeElement) {
        List<String> attachmentFilePathList = new ArrayList<>();
        List<Element> parentElementList = parentHashTreeElement.elements();
        for (Element parentElement: parentElementList) {
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
                                        if(file.exists() && file.isFile()){
                                            attachmentFilePathList.add(filePath);
                                            String fileName = file.getName();
                                            elementProp.attribute("name").setText(fileName);
                                        }
                                    }catch (Exception e){
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return attachmentFilePathList;
    }

    private void updateDubboDefaultConfigGuiElement(Element configTestElement) {
        String dubboDefaultConfigGuiClassName = "io.github.ningyu.jmeter.plugin.dubbo.gui.DubboDefaultConfigGui";
        if (configTestElement == null) {
            return;
        }
        String guiClassValue = configTestElement.attributeValue("guiclass");
        if (StringUtils.equals(guiClassValue, "DubboDefaultConfigGui")) {
            configTestElement.attribute("guiclass").setText(dubboDefaultConfigGuiClassName);
        }
    }

    private void checkPrivateFunctionNode(Element element) {
        List<Element> scriptHashTreeElementList = element.elements("hashTree");
        for (Element scriptHashTreeElement : scriptHashTreeElementList) {
            boolean isRemove = false;
            List<Element> removeElement = new ArrayList<>();
            List<Element> scriptElementItemList = scriptHashTreeElement.elements();
            for (Element hashTreeItemElement : scriptElementItemList) {
                String className = hashTreeItemElement.attributeValue("testclass");
                String qname = hashTreeItemElement.getQName().getName();

                if (isRemove) {
                    if (org.apache.commons.lang3.StringUtils.equals("hashTree", qname)) {
                        removeElement.add(hashTreeItemElement);
                    }
                }

                isRemove = false;
                if (org.apache.commons.lang3.StringUtils.equals(className, "JSR223PostProcessor")) {
                    List<Element> scriptElements = hashTreeItemElement.elements("stringProp");
                    for (Element scriptElement : scriptElements) {
                        String scriptName = scriptElement.attributeValue("name");
                        String contentValue = scriptElement.getStringValue();

                        if ("script".equals(scriptName) && contentValue.startsWith("io.metersphere.api.jmeter.JMeterVars.addVars")) {
                            isRemove = true;
                            removeElement.add(hashTreeItemElement);
                        }
                    }
                }
            }
            for (Element itemElement : removeElement) {
                scriptHashTreeElement.remove(itemElement);
            }
        }
    }

    private void updateRequestElementInfo(Element element, String testNameParam, String[] requestElementNameArr, boolean isFromScenario) {
        String attribute_testName = "testname";
        String scenarioCaseNameSplit = "<->";
        String testName = testNameParam;

        for (String requestElementName : requestElementNameArr) {
            List<Element> sampleProxyElementList = element.elements(requestElementName);
            for (Element itemElement : sampleProxyElementList) {
                if (isFromScenario) {
                    testName = itemElement.attributeValue(attribute_testName);
                    if (StringUtils.isNotBlank(testName)) {
                        String[] testNameArr = testName.split(scenarioCaseNameSplit);
                        if (testNameArr.length > 0) {
                            testName = testNameArr[0];
                        }
                    }
                }
                itemElement.attribute(attribute_testName).setText(testName);

                //double的话有额外处理方式
                if (StringUtils.equals(requestElementName, "DubboSample")) {
                    //dubbo节点要更新 标签、guiClass 和 testClass
                    itemElement.setName("io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample");
                    itemElement.attribute("testclass").setText("io.github.ningyu.jmeter.plugin.dubbo.sample.DubboSample");
                    itemElement.attribute("guiclass").setText("io.github.ningyu.jmeter.plugin.dubbo.gui.DubboSampleGui");
                }

            }
        }
    }
}
