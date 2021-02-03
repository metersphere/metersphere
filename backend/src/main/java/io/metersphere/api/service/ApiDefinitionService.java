package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.APIReportResult;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.automation.ApiScenarioRequest;
import io.metersphere.api.dto.automation.ReferenceDTO;
import io.metersphere.api.dto.datacount.ApiDataCountResult;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.definition.parse.ApiDefinitionImport;
import io.metersphere.api.dto.definition.request.ScheduleInfoSwaggerUrlRequest;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.scenario.request.RequestType;
import io.metersphere.api.dto.swaggerurl.SwaggerTaskResult;
import io.metersphere.api.dto.swaggerurl.SwaggerUrlRequest;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.api.jmeter.RequestResult;
import io.metersphere.api.jmeter.TestResult;
import io.metersphere.api.parse.ApiImportParser;
import io.metersphere.api.parse.ApiImportParserFactory;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.*;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.i18n.Translator;
import io.metersphere.job.sechedule.SwaggerUrlImportJob;
import io.metersphere.service.FileService;
import io.metersphere.service.ScheduleService;
import io.metersphere.track.request.testcase.ApiCaseRelevanceRequest;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jorphan.collections.HashTree;
import org.aspectj.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sun.security.util.Cache;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiDefinitionService {
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;
    @Resource
    private ApiTestFileMapper apiTestFileMapper;
    @Resource
    private FileService fileService;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private ExtApiDefinitionExecResultMapper extApiDefinitionExecResultMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private SwaggerUrlProjectMapper swaggerUrlProjectMapper;
    @Resource
    private ExtSwaggerUrlScheduleMapper extSwaggerUrlScheduleMapper;
    @Resource
    private ScheduleMapper scheduleMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;

    private static Cache cache = Cache.newHardMemoryCache(0, 3600 * 24);

    private static final String BODY_FILE_DIR = "/opt/metersphere/data/body";

    public List<ApiDefinitionResult> list(ApiDefinitionRequest request) {
        request = this.initRequest(request, true, true);
        List<ApiDefinitionResult> resList = extApiDefinitionMapper.list(request);
        calculateResult(resList);
        return resList;
    }

    /**
     * 初始化部分参数
     *
     * @param request
     * @param setDefultOrders
     * @param checkThisWeekData
     * @return
     */
    private ApiDefinitionRequest initRequest(ApiDefinitionRequest request, boolean setDefultOrders, boolean checkThisWeekData) {
        if (setDefultOrders) {
            request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        }
        if (checkThisWeekData) {
            if (request.isSelectThisWeedData()) {
                Map<String, Date> weekFirstTimeAndLastTime = DateUtils.getWeedFirstTimeAndLastTime(new Date());
                Date weekFirstTime = weekFirstTimeAndLastTime.get("firstTime");
                if (weekFirstTime != null) {
                    request.setCreateTime(weekFirstTime.getTime());
                }
            }
        }
        return request;
    }

    public ApiDefinition get(String id) {
        return apiDefinitionMapper.selectByPrimaryKey(id);
    }

    public ApiDefinitionWithBLOBs getBLOBs(String id) {
        return apiDefinitionMapper.selectByPrimaryKey(id);
    }

    public void create(SaveApiDefinitionRequest request, List<MultipartFile> bodyFiles) {
        List<String> bodyUploadIds = new ArrayList<>(request.getBodyUploadIds());
        createTest(request);
        createBodyFiles(bodyUploadIds, bodyFiles);
    }

    public void update(SaveApiDefinitionRequest request, List<MultipartFile> bodyFiles) {
        if (request.getRequest() != null) {
            deleteFileByTestId(request.getRequest().getId());
        }
        List<String> bodyUploadIds = request.getBodyUploadIds();
        request.setBodyUploadIds(null);
        updateTest(request);
        createBodyFiles(bodyUploadIds, bodyFiles);
    }

    public void createBodyFiles(List<String> bodyUploadIds, List<MultipartFile> bodyFiles) {
        if (CollectionUtils.isNotEmpty(bodyUploadIds) && CollectionUtils.isNotEmpty(bodyFiles)) {
            File testDir = new File(BODY_FILE_DIR);
            if (!testDir.exists()) {
                testDir.mkdirs();
            }
            for (int i = 0; i < bodyUploadIds.size(); i++) {
                MultipartFile item = bodyFiles.get(i);
                File file = new File(BODY_FILE_DIR + "/" + bodyUploadIds.get(i) + "_" + item.getOriginalFilename());
                try (InputStream in = item.getInputStream(); OutputStream out = new FileOutputStream(file)) {
                    file.createNewFile();
                    FileUtil.copyStream(in, out);
                } catch (IOException e) {
                    LogUtil.error(e);
                    MSException.throwException(Translator.get("upload_fail"));
                }
            }
        }
    }

    public void delete(String apiId) {
        apiTestCaseService.deleteTestCase(apiId);
        deleteFileByTestId(apiId);
        extApiDefinitionExecResultMapper.deleteByResourceId(apiId);
        apiDefinitionMapper.deleteByPrimaryKey(apiId);
        deleteBodyFiles(apiId);
    }

    public void deleteBatch(List<String> apiIds) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(apiIds);
        apiDefinitionMapper.deleteByExample(example);
    }

    public void removeToGc(List<String> apiIds) {
        extApiDefinitionMapper.removeToGc(apiIds);
    }

    public void reduction(List<SaveApiDefinitionRequest> requests) {
        List<String> apiIds = new ArrayList<>();
        requests.forEach(item -> {
            checkNameExist(item);
            apiIds.add(item.getId());
        });
        extApiDefinitionMapper.reduction(apiIds);
    }

    public void deleteBodyFiles(String apiId) {
        File file = new File(BODY_FILE_DIR + "/" + apiId);
        FileUtil.deleteContents(file);
        if (file.exists()) {
            file.delete();
        }
    }

    private void checkNameExist(SaveApiDefinitionRequest request) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        if (request.getProtocol().equals(RequestType.HTTP)) {
            example.createCriteria().andMethodEqualTo(request.getMethod()).andStatusNotEqualTo("Trash")
                    .andProtocolEqualTo(request.getProtocol()).andPathEqualTo(request.getPath())
                    .andProjectIdEqualTo(request.getProjectId()).andIdNotEqualTo(request.getId());
            Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
            if (apiDefinitionMapper.countByExample(example) > 0 && (project.getRepeatable() == null || !project.getRepeatable())) {
                MSException.throwException(Translator.get("api_definition_url_not_repeating"));
            }
        } else {
            example.createCriteria().andProtocolEqualTo(request.getProtocol()).andStatusNotEqualTo("Trash")
                    .andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId())
                    .andIdNotEqualTo(request.getId());
            if (apiDefinitionMapper.countByExample(example) > 0) {
                MSException.throwException(Translator.get("load_test_already_exists"));
            }
        }
    }

    private List<ApiDefinition> getSameRequest(SaveApiDefinitionRequest request) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        if (request.getProtocol().equals(RequestType.HTTP)) {
            example.createCriteria().andMethodEqualTo(request.getMethod()).andStatusNotEqualTo("Trash")
                    .andProtocolEqualTo(request.getProtocol()).andPathEqualTo(request.getPath())
                    .andProjectIdEqualTo(request.getProjectId()).andIdNotEqualTo(request.getId());
            return apiDefinitionMapper.selectByExample(example);
        } else {
            example.createCriteria().andProtocolEqualTo(request.getProtocol()).andStatusNotEqualTo("Trash")
                    .andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId())
                    .andIdNotEqualTo(request.getId());
            return apiDefinitionMapper.selectByExample(example);
        }
    }

    private ApiDefinition updateTest(SaveApiDefinitionRequest request) {
        checkNameExist(request);
        final ApiDefinitionWithBLOBs test = new ApiDefinitionWithBLOBs();
        test.setId(request.getId());
        test.setName(request.getName());
        test.setPath(request.getPath());
        test.setProjectId(request.getProjectId());
        request.getRequest().setId(request.getId());
        test.setRequest(JSONObject.toJSONString(request.getRequest()));
        test.setUpdateTime(System.currentTimeMillis());
        test.setStatus(request.getStatus());
        test.setModulePath(request.getModulePath());
        test.setModuleId(request.getModuleId());
        test.setMethod(request.getMethod());
        test.setProtocol(request.getProtocol());
        test.setDescription(request.getDescription());
        test.setResponse(JSONObject.toJSONString(request.getResponse()));
        test.setEnvironmentId(request.getEnvironmentId());
        test.setUserId(request.getUserId());
        test.setTags(request.getTags());

        apiDefinitionMapper.updateByPrimaryKeySelective(test);
        return test;
    }

    private ApiDefinition createTest(SaveApiDefinitionRequest request) {
        checkNameExist(request);
        final ApiDefinitionWithBLOBs test = new ApiDefinitionWithBLOBs();
        test.setId(request.getId());
        test.setName(request.getName());
        test.setProtocol(request.getProtocol());
        test.setMethod(request.getMethod());
        test.setPath(request.getPath());
        test.setModuleId(request.getModuleId());
        test.setProjectId(request.getProjectId());
        request.getRequest().setId(request.getId());
        test.setRequest(JSONObject.toJSONString(request.getRequest()));
        test.setCreateTime(System.currentTimeMillis());
        test.setUpdateTime(System.currentTimeMillis());
        test.setStatus(APITestStatus.Underway.name());
        test.setModulePath(request.getModulePath());
        test.setResponse(JSONObject.toJSONString(request.getResponse()));
        test.setEnvironmentId(request.getEnvironmentId());
        test.setNum(getNextNum(request.getProjectId()));
        if (request.getUserId() == null) {
            test.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        } else {
            test.setUserId(request.getUserId());
        }
        test.setDescription(request.getDescription());
        test.setTags(request.getTags());
        apiDefinitionMapper.insert(test);
        return test;
    }

    private int getNextNum(String projectId) {
        ApiDefinition apiDefinition = extApiDefinitionMapper.getNextNum(projectId);
        if (apiDefinition == null) {
            return 100001;
        } else {
            return Optional.of(apiDefinition.getNum() + 1).orElse(100001);
        }
    }

    private ApiDefinition importCreate(ApiDefinitionResult request, ApiDefinitionMapper batchMapper, ApiTestCaseMapper apiTestCaseMapper, ApiTestImportRequest apiTestImportRequest) {
        SaveApiDefinitionRequest saveReq = new SaveApiDefinitionRequest();
        BeanUtils.copyBean(saveReq, request);
        final ApiDefinitionWithBLOBs apiDefinition = new ApiDefinitionWithBLOBs();
        BeanUtils.copyBean(apiDefinition, request);
        apiDefinition.setCreateTime(System.currentTimeMillis());
        apiDefinition.setUpdateTime(System.currentTimeMillis());
        apiDefinition.setStatus(APITestStatus.Underway.name());
        if (request.getUserId() == null) {
            apiDefinition.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        } else {
            apiDefinition.setUserId(request.getUserId());
        }
        apiDefinition.setDescription(request.getDescription());

        List<ApiDefinition> sameRequest = getSameRequest(saveReq);
        if (StringUtils.equals("fullCoverage", apiTestImportRequest.getModeId())) {
            _importCreate(sameRequest, batchMapper, apiDefinition, apiTestCaseMapper, apiTestImportRequest);
        } else if (StringUtils.equals("incrementalMerge", apiTestImportRequest.getModeId())) {
            if (CollectionUtils.isEmpty(sameRequest)) {
                //postman 可能含有前置脚本，接口定义去掉脚本
                String requestStr = setImportHashTree(apiDefinition);
                batchMapper.insert(apiDefinition);
                apiDefinition.setRequest(requestStr);
                importApiCase(apiDefinition, apiTestCaseMapper, apiTestImportRequest, true);
            }
        } else {
            _importCreate(sameRequest, batchMapper, apiDefinition, apiTestCaseMapper, apiTestImportRequest);
        }
        return apiDefinition;
    }

    private void _importCreate(List<ApiDefinition> sameRequest, ApiDefinitionMapper batchMapper, ApiDefinitionWithBLOBs apiDefinition,
                               ApiTestCaseMapper apiTestCaseMapper, ApiTestImportRequest apiTestImportRequest) {
        if (CollectionUtils.isEmpty(sameRequest)) {
            String request = setImportHashTree(apiDefinition);
            batchMapper.insert(apiDefinition);
            apiDefinition.setRequest(request);
            importApiCase(apiDefinition, apiTestCaseMapper, apiTestImportRequest, true);
        } else {
            //如果存在则修改
            apiDefinition.setId(sameRequest.get(0).getId());
            String request = setImportHashTree(apiDefinition);
            apiDefinitionMapper.updateByPrimaryKeyWithBLOBs(apiDefinition);
            apiDefinition.setRequest(request);
            importApiCase(apiDefinition, apiTestCaseMapper, apiTestImportRequest, false);
        }
    }

    private String setImportHashTree(ApiDefinitionWithBLOBs apiDefinition) {
        String request = apiDefinition.getRequest();
        MsHTTPSamplerProxy msHTTPSamplerProxy = JSONObject.parseObject(request, MsHTTPSamplerProxy.class);
        msHTTPSamplerProxy.setId(apiDefinition.getId());
        msHTTPSamplerProxy.setHashTree(new LinkedList<>());
        apiDefinition.setRequest(JSONObject.toJSONString(msHTTPSamplerProxy));
        return request;
    }

   /**
    * 导入是插件或者postman时创建用例
    * postman考虑是否有前置脚本
    */
    private void importApiCase(ApiDefinitionWithBLOBs apiDefinition, ApiTestCaseMapper apiTestCaseMapper,
                               ApiTestImportRequest apiTestImportRequest, Boolean isInsert) {
        try {
            if (StringUtils.equalsAnyIgnoreCase(apiTestImportRequest.getPlatform(), ApiImportPlatform.Plugin.name(), ApiImportPlatform.Postman.name())) {
                ApiTestCaseWithBLOBs apiTestCase = new ApiTestCaseWithBLOBs();
                BeanUtils.copyBean(apiTestCase, apiDefinition);
                apiTestCase.setId(UUID.randomUUID().toString());
                apiTestCase.setApiDefinitionId(apiDefinition.getId());
                apiTestCase.setCreateTime(System.currentTimeMillis());
                apiTestCase.setUpdateTime(System.currentTimeMillis());
                apiTestCase.setCreateUserId(SessionUtils.getUserId());
                apiTestCase.setUpdateUserId(SessionUtils.getUserId());
                apiTestCase.setPriority("P0");
                if (!isInsert) {
                    apiTestCase.setName(apiTestCase.getName() + "_" + apiTestCase.getId().substring(0, 5));
                }
                apiTestCaseMapper.insert(apiTestCase);
            }
        } catch (Exception e) {
            LogUtil.error("导入创建用例异常", e);
        }
    }

    private void deleteFileByTestId(String apiId) {
        ApiTestFileExample apiTestFileExample = new ApiTestFileExample();
        apiTestFileExample.createCriteria().andTestIdEqualTo(apiId);
        final List<ApiTestFile> ApiTestFiles = apiTestFileMapper.selectByExample(apiTestFileExample);
        apiTestFileMapper.deleteByExample(apiTestFileExample);

        if (!CollectionUtils.isEmpty(ApiTestFiles)) {
            final List<String> fileIds = ApiTestFiles.stream().map(ApiTestFile::getFileId).collect(Collectors.toList());
            fileService.deleteFileByIds(fileIds);
        }
    }

    /**
     * 测试执行
     *
     * @param request
     * @param bodyFiles
     * @return
     */
    public String run(RunDefinitionRequest request, List<MultipartFile> bodyFiles) {
        List<String> bodyUploadIds = new ArrayList<>(request.getBodyUploadIds());
        createBodyFiles(bodyUploadIds, bodyFiles);

        HashTree hashTree = request.getTestElement().generateHashTree();
        String runMode = ApiRunMode.DEFINITION.name();
        if (StringUtils.isNotBlank(request.getType()) && StringUtils.equals(request.getType(), ApiRunMode.API_PLAN.name())) {
            runMode = ApiRunMode.API_PLAN.name();
        }
        // 调用执行方法
        jMeterService.runDefinition(request.getId(), hashTree, request.getReportId(), runMode);
        return request.getId();
    }

    public void addResult(TestResult res) {
        if (!res.getScenarios().isEmpty() && !res.getScenarios().get(0).getRequestResults().isEmpty()) {
            RequestResult result = res.getScenarios().get(0).getRequestResults().get(0);
            if (result.getName().indexOf("<->") != -1) {
                result.setName(result.getName().substring(0, result.getName().indexOf("<->")));
            }
            cache.put(res.getTestId(), result);
        } else {
            MSException.throwException(Translator.get("test_not_found"));
        }
    }

    /**
     * 获取零时执行结果报告
     *
     * @param testId
     * @param test
     * @return
     */
    public APIReportResult getResult(String testId, String test) {
        Object res = cache.get(testId);
        if (res != null) {
            cache.remove(testId);
            APIReportResult reportResult = new APIReportResult();
            reportResult.setContent(JSON.toJSONString(res));
            return reportResult;
        }
        return null;
    }

    /**
     * 获取存储执行结果报告
     *
     * @param testId
     * @return
     */
    public APIReportResult getDbResult(String testId) {
        ApiDefinitionExecResult result = extApiDefinitionExecResultMapper.selectMaxResultByResourceId(testId);
        return buildAPIReportResult(result);
    }

    private APIReportResult buildAPIReportResult(ApiDefinitionExecResult result) {
        if (result == null) {
            return null;
        }
        APIReportResult reportResult = new APIReportResult();
        reportResult.setContent(result.getContent());
        return reportResult;
    }

    public APIReportResult getDbResult(String testId, String type) {
        ApiDefinitionExecResult result = extApiDefinitionExecResultMapper.selectMaxResultByResourceIdAndType(testId, type);
        return buildAPIReportResult(result);
    }

    public ApiDefinitionExecResult getResultByJenkins(String testId, String type) {
        return extApiDefinitionExecResultMapper.selectMaxResultByResourceIdAndType(testId, type);
    }

    public ApiTestCaseWithBLOBs getApiCaseInfo(String apiCaseId) {
        return apiTestCaseMapper.selectByPrimaryKey(apiCaseId);
    }


    public ApiDefinitionImport apiTestImport(MultipartFile file, ApiTestImportRequest request) {
        ApiImportParser apiImportParser = ApiImportParserFactory.getApiImportParser(request.getPlatform());
        ApiDefinitionImport apiImport = null;
        try {
            apiImport = Objects.requireNonNull(apiImportParser).parse(file == null ? null : file.getInputStream(), request);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            MSException.throwException(Translator.get("parse_data_error"));
        }
        if (request.isSaved()) {
            importApi(request, apiImport);
        }
        return apiImport;
    }

    private void importApi(ApiTestImportRequest request, ApiDefinitionImport apiImport) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDefinitionMapper batchMapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        ApiTestCaseMapper apiTestCaseMapper = sqlSession.getMapper(ApiTestCaseMapper.class);
        List<ApiDefinitionResult> data = apiImport.getData();
        int num = 0;
        if (!CollectionUtils.isEmpty(data) && data.get(0) != null && data.get(0).getProjectId() != null) {
            num = getNextNum(data.get(0).getProjectId());
        }
        for (int i = 0; i < data.size(); i++) {
            ApiDefinitionResult item = data.get(i);
            if (item.getName().length() > 255) {
                item.setName(item.getName().substring(0, 255));
            }
            item.setNum(num++);
            importCreate(item, batchMapper, apiTestCaseMapper, request);
            if (i % 300 == 0) {
                sqlSession.flushStatements();
            }
        }
        sqlSession.flushStatements();
    }

    public ReferenceDTO getReference(ApiScenarioRequest request) {
        ReferenceDTO dto = new ReferenceDTO();
        dto.setScenarioList(extApiScenarioMapper.selectReference(request));
        QueryTestPlanRequest planRequest = new QueryTestPlanRequest();
        planRequest.setApiId(request.getId());
        planRequest.setProjectId(request.getProjectId());
        dto.setTestPlanList(extTestPlanMapper.selectTestPlanByRelevancy(planRequest));
        return dto;
    }

    public void editApiBath(ApiBatchRequest request) {
        ApiDefinitionExample definitionExample = new ApiDefinitionExample();
        definitionExample.createCriteria().andIdIn(request.getIds());

        ApiDefinitionWithBLOBs definitionWithBLOBs = new ApiDefinitionWithBLOBs();
        BeanUtils.copyBean(definitionWithBLOBs, request);
        definitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
        apiDefinitionMapper.updateByExampleSelective(definitionWithBLOBs, definitionExample);
    }

    public void editApiByParam(ApiBatchRequest request) {
        List<String> ids = request.getIds();
        if (request.isSelectAllDate()) {
            ids = this.getAllApiIdsByFontedSelect(request.getFilters(), request.getName(), request.getModuleIds(), request.getProjectId(), request.getUnSelectIds(),request.getProtocol());
        }
        //name在这里只是查询参数
        request.setName(null);

        ApiDefinitionExample definitionExample = new ApiDefinitionExample();
        definitionExample.createCriteria().andIdIn(ids);

        ApiDefinitionWithBLOBs definitionWithBLOBs = new ApiDefinitionWithBLOBs();
        BeanUtils.copyBean(definitionWithBLOBs, request);
        definitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
        apiDefinitionMapper.updateByExampleSelective(definitionWithBLOBs, definitionExample);
    }

    public void testPlanRelevance(ApiCaseRelevanceRequest request) {
        apiTestCaseService.relevanceByApi(request);
    }

    /**
     * 数据统计-接口类型
     *
     * @param projectId 项目ID
     * @return
     */
    public List<ApiDataCountResult> countProtocolByProjectID(String projectId) {
        return extApiDefinitionMapper.countProtocolByProjectID(projectId);
    }

    /**
     * 统计本周创建的数据总量
     *
     * @param projectId
     * @return
     */
    public long countByProjectIDAndCreateInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiDefinitionMapper.countByProjectIDAndCreateInThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<ApiDataCountResult> countStateByProjectID(String projectId) {
        return extApiDefinitionMapper.countStateByProjectID(projectId);
    }

    public List<ApiDataCountResult> countApiCoverageByProjectID(String projectId) {
        return extApiDefinitionMapper.countApiCoverageByProjectID(projectId);
    }

    public List<ApiDefinition> selectApiDefinitionBydIds(List<String> ids) {
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(ids);
        return apiDefinitionMapper.selectByExample(example);
    }

    public void deleteByParams(ApiDefinitionBatchProcessingRequest request) {
        List<String> apiIds = request.getDataIds();
        if (request.isSelectAllDate()) {
            apiIds = this.getAllApiIdsByFontedSelect(request.getFilters(), request.getName(), request.getModuleIds(), request.getProjectId(), request.getUnSelectIds(),request.getProtocol());
        }
        ApiDefinitionExample example = new ApiDefinitionExample();
        example.createCriteria().andIdIn(apiIds);
        apiDefinitionMapper.deleteByExample(example);
    }

    private List<String> getAllApiIdsByFontedSelect(Map<String, List<String>> filters, String name, List<String> moduleIds, String projectId, List<String> unSelectIds,String protocol) {
        ApiDefinitionRequest request = new ApiDefinitionRequest();
        request.setFilters(filters);
        request.setName(name);
        request.setModuleIds(moduleIds);
        request.setProjectId(projectId);
        request.setWorkspaceId(SessionUtils.getCurrentWorkspaceId());
        request.setProtocol(protocol);
        List<ApiDefinitionResult> resList = extApiDefinitionMapper.list(request);
        List<String> ids = new ArrayList<>(0);
        if (!resList.isEmpty()) {
            List<String> allIds = resList.stream().map(ApiDefinitionResult::getId).collect(Collectors.toList());
            ids = allIds.stream().filter(id -> !unSelectIds.contains(id)).collect(Collectors.toList());
        }
        return ids;
    }

    public void removeToGcByParams(ApiDefinitionBatchProcessingRequest request) {
        List<String> apiIds = request.getDataIds();
        if (request.isSelectAllDate()) {
            apiIds = this.getAllApiIdsByFontedSelect(request.getFilters(), request.getName(), request.getModuleIds(), request.getProjectId(), request.getUnSelectIds(),request.getProtocol());
        }
        extApiDefinitionMapper.removeToGc(apiIds);
    }

    public List<ApiDefinitionResult> listRelevance(ApiDefinitionRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<ApiDefinitionResult> resList = extApiDefinitionMapper.listRelevance(request);
        calculateResult(resList);
        return resList;
    }

    public void calculateResult(List<ApiDefinitionResult> resList) {
        if (!resList.isEmpty()) {
            List<String> ids = resList.stream().map(ApiDefinitionResult::getId).collect(Collectors.toList());
            List<ApiComputeResult> results = extApiDefinitionMapper.selectByIds(ids);
            Map<String, ApiComputeResult> resultMap = results.stream().collect(Collectors.toMap(ApiComputeResult::getApiDefinitionId, Function.identity()));
            for (ApiDefinitionResult res : resList) {
                ApiComputeResult compRes = resultMap.get(res.getId());
                if (compRes != null) {
                    res.setCaseTotal(String.valueOf(compRes.getCaseTotal()));
                    res.setCasePassingRate(compRes.getPassRate());
                    // 状态优先级 未执行，未通过，通过
                    if ((compRes.getError() + compRes.getSuccess()) < compRes.getCaseTotal()) {
                        res.setCaseStatus("未执行");
                    } else if (compRes.getError() > 0) {
                        res.setCaseStatus("未通过");
                    } else {
                        res.setCaseStatus("通过");
                    }
                } else {
                    res.setCaseTotal("-");
                    res.setCasePassingRate("-");
                    res.setCaseStatus("-");
                }
            }
        }
    }

    /*swagger定时导入*/
    public void createSchedule(Schedule request) {
        /*保存swaggerUrl*/
        SwaggerUrlProject swaggerUrlProject = new SwaggerUrlProject();
        swaggerUrlProject.setId(UUID.randomUUID().toString());
        swaggerUrlProject.setProjectId(request.getProjectId());
        swaggerUrlProject.setSwaggerUrl(request.getResourceId());
        swaggerUrlProject.setModuleId(request.getModuleId());
        swaggerUrlProject.setModulePath(request.getModulePath());
        swaggerUrlProject.setModeId(request.getModeId());
        scheduleService.addSwaggerUrlSchedule(swaggerUrlProject);
        request.setResourceId(swaggerUrlProject.getId());
        Schedule schedule = scheduleService.buildApiTestSchedule(request);
        schedule.setJob(SwaggerUrlImportJob.class.getName());
        schedule.setGroup(ScheduleGroup.SWAGGER_IMPORT.name());
        schedule.setType(ScheduleType.CRON.name());
        scheduleService.addSchedule(schedule);
        this.addOrUpdateSwaggerImportCronJob(request);

    }

    //关闭
    public void updateSchedule(Schedule request) {
        scheduleService.editSchedule(request);
        this.addOrUpdateSwaggerImportCronJob(request);
    }

    //删除
    public void deleteSchedule(ScheduleInfoSwaggerUrlRequest request) {
        swaggerUrlProjectMapper.deleteByPrimaryKey(request.getId());
        scheduleMapper.deleteByPrimaryKey(request.getTaskId());

    }

    //查询swaggerUrl详情
    public SwaggerUrlProject getSwaggerInfo(String resourceId) {
        return swaggerUrlProjectMapper.selectByPrimaryKey(resourceId);
    }

    public String getResourceId(SwaggerUrlRequest swaggerUrlRequest) {
        SwaggerUrlProjectExample swaggerUrlProjectExample = new SwaggerUrlProjectExample();
        SwaggerUrlProjectExample.Criteria criteria = swaggerUrlProjectExample.createCriteria();
        criteria.andProjectIdEqualTo(swaggerUrlRequest.getProjectId()).andSwaggerUrlEqualTo(swaggerUrlRequest.getSwaggerUrl());
        if (StringUtils.isNotBlank(swaggerUrlRequest.getModuleId())) {
            criteria.andModuleIdEqualTo(swaggerUrlRequest.getModuleId());
        }
        List<SwaggerUrlProject> list = swaggerUrlProjectMapper.selectByExample(swaggerUrlProjectExample);
        String resourceId = "";
        if (list.size() == 1) {
            resourceId = list.get(0).getId();
        }
        return resourceId;
    }

    public List<SwaggerTaskResult> getSwaggerScheduleList(String projectId) {
        return extSwaggerUrlScheduleMapper.getSwaggerTaskList(projectId);
    }

    private void addOrUpdateSwaggerImportCronJob(Schedule request) {
        scheduleService.addOrUpdateCronJob(request, SwaggerUrlImportJob.getJobKey(request.getResourceId()), SwaggerUrlImportJob.getTriggerKey(request.getResourceId()), SwaggerUrlImportJob.class);
    }
}
