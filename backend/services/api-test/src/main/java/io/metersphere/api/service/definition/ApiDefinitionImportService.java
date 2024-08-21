package io.metersphere.api.service.definition;

import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.constants.ApiImportPlatform;
import io.metersphere.api.domain.*;
import io.metersphere.api.dto.converter.*;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.MsHeader;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.api.dto.schema.JsonSchemaItem;
import io.metersphere.api.mapper.*;
import io.metersphere.api.parser.ApiDefinitionImportParser;
import io.metersphere.api.parser.ImportParserFactory;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.api.utils.ApiDefinitionImportUtils;
import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.metersphere.project.api.KeyValueEnableParam;
import io.metersphere.project.constants.PropertyConstant;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.*;
import io.metersphere.system.domain.User;
import io.metersphere.system.dto.sdk.ApiDefinitionCaseDTO;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.CommonNoticeSendService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import io.metersphere.system.utils.ServiceUtils;
import io.metersphere.system.utils.TreeNodeParseUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static io.metersphere.project.utils.NodeSortUtils.DEFAULT_NODE_INTERVAL_POS;

@Service
public class ApiDefinitionImportService {

    private static final String UNPLANNED_API = "api_unplanned_request";
    private final ThreadLocal<Long> currentApiOrder = new ThreadLocal<>();
    private final ThreadLocal<Long> currentApiCaseOrder = new ThreadLocal<>();
    private final ThreadLocal<Long> currentModuleOrder = new ThreadLocal<>();
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private ExtBaseProjectVersionMapper extBaseProjectVersionMapper;
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;
    @Resource
    private ExtApiDefinitionModuleMapper extApiDefinitionModuleMapper;
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ApiDefinitionBlobMapper apiDefinitionBlobMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ApiDefinitionModuleService apiDefinitionModuleService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private CommonNoticeSendService commonNoticeSendService;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ApiDefinitionModuleMapper apiDefinitionModuleMapper;
    @Resource
    private ApiDefinitionMockMapper apiDefinitionMockMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;

    private void initImportRequestAndCheck(MultipartFile file, ImportRequest request, String projectId) {
        if (StringUtils.isBlank(request.getProjectId())) {
            request.setProjectId(projectId);
        }
        //判断是否是定时任务进入
        if (StringUtils.equals(request.getType(), "SCHEDULE")) {
            request.setProtocol(ModuleConstants.NODE_PROTOCOL_HTTP);
        }

        if (file != null) {
            String originalFilename = file.getOriginalFilename();
            if (StringUtils.isNotBlank(originalFilename)) {
                String suffixName = originalFilename.substring(originalFilename.indexOf(".") + 1);
                ApiDefinitionImportUtils.checkFileSuffixName(request, suffixName);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void apiDefinitionImport(MultipartFile file, ImportRequest request, String projectId) {
        this.initImportRequestAndCheck(file, request, projectId);
        ApiDefinitionImportParser<?> runService = ImportParserFactory.getImportParser(request.getPlatform());
        assert runService != null;
        ApiImportDataAnalysisResult apiImportDataAnalysisResult;
        try {
            //解析文件
            ApiImportFileParseResult fileParseResult = (ApiImportFileParseResult) runService.parse(file == null ? null : file.getInputStream(), request);

            ApiDefinitionPageRequest pageRequest = new ApiDefinitionPageRequest();
            pageRequest.setProjectId(request.getProjectId());
            pageRequest.setProtocols(Collections.singletonList(runService.getParseProtocol()));
            List<ApiDefinitionDetail> existenceApiDefinitionList = extApiDefinitionMapper.importList(pageRequest);
            //分析有哪些数据需要新增、有哪些数据需要更新
            apiImportDataAnalysisResult = runService.generateInsertAndUpdateData(fileParseResult, existenceApiDefinitionList);
        } catch (Exception e) {
            LogUtils.error(e.getMessage(), e);
            throw new MSException(Translator.get("parse_data_error"));
        }

        try {
            //初始化版本信息，用于保存，以及以后真对具体版本导入进行拓展
            String defaultVersion = extBaseProjectVersionMapper.getDefaultVersion(request.getProjectId());
            request.setDefaultVersion(defaultVersion);
            if (request.getVersionId() == null) {
                request.setVersionId(defaultVersion);
            }
            //通过导入配置，预处理数据，确定哪些要创建、哪些要修改
            ApiDefinitionPreImportAnalysisResult preImportAnalysisResult = this.preImportAnalysis(request, apiImportDataAnalysisResult);
            //入库
            List<LogDTO> operationLogs = this.insertData(preImportAnalysisResult, request);
            operationLogService.batchAdd(operationLogs);
        } catch (Exception e) {
            LogUtils.error(e);
            throw new MSException(Translator.get("user_import_format_wrong"));
        }
    }

    public Long getNextOrder(String projectId) {
        Long pos = extApiDefinitionMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + DEFAULT_NODE_INTERVAL_POS;
    }

    public Long getImportNextOrder(String projectId) {
        Long order = currentApiOrder.get();
        if (order == null) {
            order = getNextOrder(projectId);
        }
        order = order + DEFAULT_NODE_INTERVAL_POS;
        currentApiOrder.set(order);
        return order;
    }

    public Long getImportNextModuleOrder(String projectId) {
        Long order = currentModuleOrder.get();
        if (order == null) {
            order = apiDefinitionModuleService.getNextOrder(projectId);
        }
        order = order + DEFAULT_NODE_INTERVAL_POS;
        currentModuleOrder.set(order);
        return order;
    }

    public List<LogDTO> insertData(
            ApiDefinitionPreImportAnalysisResult apiDefinitionPreImportAnalysisResult,
            ImportRequest request) {
        List<LogDTO> operationLogs = new ArrayList<>();
        currentApiCaseOrder.remove();
        currentApiOrder.remove();
        currentModuleOrder.remove();

        //更新、修改数据
        {
            SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
            ApiDefinitionModuleMapper batchApiModuleMapper = sqlSession.getMapper(ApiDefinitionModuleMapper.class);
            ApiDefinitionMapper batchApiDefinitionMapper = sqlSession.getMapper(ApiDefinitionMapper.class);
            ApiDefinitionBlobMapper batchApiBlobMapper = sqlSession.getMapper(ApiDefinitionBlobMapper.class);
            ApiTestCaseMapper batchApiCaseMapper = sqlSession.getMapper(ApiTestCaseMapper.class);
            ApiTestCaseBlobMapper batchApiCaseBlobMapper = sqlSession.getMapper(ApiTestCaseBlobMapper.class);
            ApiDefinitionMockMapper batchApiMockMapper = sqlSession.getMapper(ApiDefinitionMockMapper.class);
            ApiDefinitionMockConfigMapper batchApiMockConfigMapper = sqlSession.getMapper(ApiDefinitionMockConfigMapper.class);

            insertModule(request, apiDefinitionPreImportAnalysisResult.getInsertModuleList(), batchApiModuleMapper, sqlSession);
            updateApiDefinitionModule(request, apiDefinitionPreImportAnalysisResult.getUpdateModuleApiList(), batchApiDefinitionMapper, sqlSession);
            updateApiDefinition(request, apiDefinitionPreImportAnalysisResult.getUpdateApiData(), batchApiDefinitionMapper, batchApiBlobMapper, sqlSession);
            insertApiDefinition(request, apiDefinitionPreImportAnalysisResult.getInsertApiData(), batchApiDefinitionMapper, batchApiBlobMapper, sqlSession);
            insertApiTestCase(request, apiDefinitionPreImportAnalysisResult.getInsertApiCaseList(), batchApiCaseMapper, batchApiCaseBlobMapper, sqlSession);
            updateApiTestCase(request, apiDefinitionPreImportAnalysisResult.getUpdateApiCaseList(), batchApiCaseMapper, batchApiCaseBlobMapper, sqlSession);
            insertApiMock(request, apiDefinitionPreImportAnalysisResult.getInsertApiMockList(), batchApiDefinitionMapper, batchApiMockMapper, batchApiMockConfigMapper, sqlSession);
            updateApiMock(request, apiDefinitionPreImportAnalysisResult.getUpdateApiMockList(), batchApiMockMapper, batchApiMockConfigMapper, sqlSession);

            sqlSession.flushStatements();
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }
        //记录log以及发送通知
        {
            Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
            List<ApiDefinitionCaseDTO> noticeCreateLists = new ArrayList<>();
            List<ApiDefinitionCaseDTO> noticeUpdateLists = new ArrayList<>();
            apiDefinitionPreImportAnalysisResult.getInsertModuleList().forEach(t -> {
                operationLogs.add(ApiDefinitionImportUtils.genImportLog(project, t.getId(), t.getName(), t, OperationLogModule.API_TEST_MANAGEMENT_MODULE, request.getUserId(), OperationLogType.UPDATE.name()));
            });

            apiDefinitionPreImportAnalysisResult.getUpdateModuleApiList().forEach(t -> {
                ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
                BeanUtils.copyBean(apiDefinitionDTO, t);
                ApiDefinitionCaseDTO apiDefinitionCaseDTO = new ApiDefinitionCaseDTO();
                BeanUtils.copyBean(apiDefinitionCaseDTO, apiDefinitionDTO);
                noticeUpdateLists.add(apiDefinitionCaseDTO);
                operationLogs.add(ApiDefinitionImportUtils.genImportLog(project, t.getId(), t.getName(), apiDefinitionDTO, OperationLogModule.API_TEST_MANAGEMENT_DEFINITION, request.getUserId(), OperationLogType.UPDATE.name()));
            });
            apiDefinitionPreImportAnalysisResult.getUpdateApiData().forEach(t -> {
                ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
                BeanUtils.copyBean(apiDefinitionDTO, t);
                ApiDefinitionCaseDTO apiDefinitionCaseDTO = new ApiDefinitionCaseDTO();
                BeanUtils.copyBean(apiDefinitionCaseDTO, apiDefinitionDTO);
                noticeUpdateLists.add(apiDefinitionCaseDTO);
                operationLogs.add(ApiDefinitionImportUtils.genImportLog(project, t.getId(), t.getName(), apiDefinitionDTO, OperationLogModule.API_TEST_MANAGEMENT_DEFINITION, request.getUserId(), OperationLogType.UPDATE.name()));
            });
            apiDefinitionPreImportAnalysisResult.getInsertApiData().forEach(t -> {
                ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
                BeanUtils.copyBean(apiDefinitionDTO, t);
                ApiDefinitionCaseDTO apiDefinitionCaseDTO = new ApiDefinitionCaseDTO();
                BeanUtils.copyBean(apiDefinitionCaseDTO, apiDefinitionDTO);
                noticeCreateLists.add(apiDefinitionCaseDTO);
                operationLogs.add(ApiDefinitionImportUtils.genImportLog(project, t.getId(), t.getName(), apiDefinitionDTO, OperationLogModule.API_TEST_MANAGEMENT_DEFINITION, request.getUserId(), OperationLogType.IMPORT.name()));
            });

            //发送通知
            List<Map> createResources = new ArrayList<>(JSON.parseArray(JSON.toJSONString(noticeCreateLists), Map.class));
            User user = userMapper.selectByPrimaryKey(request.getUserId());
            commonNoticeSendService.sendNotice(NoticeConstants.TaskType.API_DEFINITION_TASK, NoticeConstants.Event.CREATE, createResources, user, request.getProjectId());
            List<Map> updateResources = new ArrayList<>(JSON.parseArray(JSON.toJSONString(noticeUpdateLists), Map.class));
            commonNoticeSendService.sendNotice(NoticeConstants.TaskType.API_DEFINITION_TASK, NoticeConstants.Event.UPDATE, updateResources, user, request.getProjectId());
        }
        return operationLogs;
    }

    private void insertApiDefinition(ImportRequest request, List<ApiDefinitionDetail> insertApiData, ApiDefinitionMapper apiMapper, ApiDefinitionBlobMapper apiBlobMapper, SqlSession sqlSession) {
        insertApiData.forEach(t -> {
            ApiDefinition apiDefinition = new ApiDefinition();
            BeanUtils.copyBean(apiDefinition, t);
            if (StringUtils.isEmpty(apiDefinition.getId())) {
                // 部分文件导入时不会预先设置id
                apiDefinition.setId(IDGenerator.nextStr());
            }

            apiDefinition.setProjectId(request.getProjectId());
            apiDefinition.setPos(getImportNextOrder(request.getProjectId()));
            apiDefinition.setNum(NumGenerator.nextNum(request.getProjectId(), ApplicationNumScope.API_DEFINITION));
            apiDefinition.setLatest(true);
            apiDefinition.setStatus(ApiDefinitionStatus.PROCESSING.name());
            apiDefinition.setRefId(apiDefinition.getId());
            apiDefinition.setVersionId(request.getVersionId());
            apiDefinition.setCreateUser(request.getUserId());
            apiDefinition.setCreateTime(System.currentTimeMillis());
            apiDefinition.setUpdateUser(request.getUserId());
            apiDefinition.setUpdateTime(System.currentTimeMillis());
            apiMapper.insertSelective(apiDefinition);
            //插入blob数据
            ApiDefinitionBlob apiDefinitionBlob = new ApiDefinitionBlob();
            apiDefinitionBlob.setId(apiDefinition.getId());
            apiDefinitionBlob.setRequest(JSON.toJSONBytes(t.getRequest()));
            apiDefinitionBlob.setResponse(JSON.toJSONBytes(t.getResponse()));
            apiBlobMapper.insertSelective(apiDefinitionBlob);
        });
        sqlSession.flushStatements();
    }

    private void insertApiTestCase(ImportRequest request, List<ApiTestCaseDTO> insertApiCaseData, ApiTestCaseMapper apiTestCaseMapper, ApiTestCaseBlobMapper apiTestCaseBlobMapper, SqlSession sqlSession) {
        insertApiCaseData.forEach(t -> {
            ApiTestCase apiTestCase = new ApiTestCase();
            BeanUtils.copyBean(apiTestCase, t);
            apiTestCase.setId(IDGenerator.nextStr());
            apiTestCase.setProjectId(request.getProjectId());
            apiTestCase.setPos(getImportNextOrder(request.getProjectId()));
            apiTestCase.setNum(NumGenerator.nextNum(request.getProjectId(), ApplicationNumScope.API_DEFINITION));
            apiTestCase.setStatus(ApiDefinitionStatus.PROCESSING.name());
            apiTestCase.setVersionId(request.getVersionId());
            apiTestCase.setCreateUser(request.getUserId());
            apiTestCase.setCreateTime(System.currentTimeMillis());
            apiTestCase.setUpdateUser(request.getUserId());
            apiTestCase.setUpdateTime(System.currentTimeMillis());
            apiTestCaseMapper.insertSelective(apiTestCase);
            //插入blob数据
            ApiTestCaseBlob apiTestCaseBlob = new ApiTestCaseBlob();
            apiTestCaseBlob.setId(apiTestCase.getId());

            apiTestCaseBlob.setRequest(getMsTestElementStr(t.getRequest()).getBytes());
            apiTestCaseBlobMapper.insertSelective(apiTestCaseBlob);
        });
        sqlSession.flushStatements();
    }

    private void insertApiMock(ImportRequest request, List<ApiDefinitionMockDTO> insertMockList, ApiDefinitionMapper batchApiMapper, ApiDefinitionMockMapper batchMockMapper, ApiDefinitionMockConfigMapper batchMockConfigMapper, SqlSession sqlSession) {
        Map<String, ApiDefinition> apiDefinitionIdMap = new HashMap<>();

        insertMockList.forEach(t -> {
            ApiDefinition api = apiDefinitionIdMap.get(t.getApiDefinitionId());
            if (api == null) {
                api = batchApiMapper.selectByPrimaryKey(t.getApiDefinitionId());
                apiDefinitionIdMap.put(t.getApiDefinitionId(), api);
            }
            ApiDefinitionMock apiMock = new ApiDefinitionMock();
            BeanUtils.copyBean(apiMock, t);
            apiMock.setId(IDGenerator.nextStr());
            apiMock.setProjectId(request.getProjectId());
            apiMock.setExpectNum(String.valueOf(NumGenerator.nextNum(request.getProjectId() + "_" + api.getNum(), ApplicationNumScope.API_MOCK)));
            apiMock.setVersionId(request.getVersionId());
            apiMock.setCreateUser(request.getUserId());
            apiMock.setCreateTime(System.currentTimeMillis());
            apiMock.setUpdateUser(request.getUserId());
            apiMock.setUpdateTime(System.currentTimeMillis());
            batchMockMapper.insertSelective(apiMock);
            //插入blob数据
            ApiDefinitionMockConfig mockConfig = new ApiDefinitionMockConfig();
            mockConfig.setId(apiMock.getId());
            mockConfig.setMatching(JSON.toJSONString(t.getMockMatchRule()).getBytes());
            mockConfig.setResponse(JSON.toJSONString(t.getResponse()).getBytes());
            batchMockConfigMapper.insertSelective(mockConfig);
        });
        sqlSession.flushStatements();
    }

    private String getMsTestElementStr(Object request) {
        String requestStr = JSON.toJSONString(request);
        AbstractMsTestElement msTestElement = ApiDataUtils.parseObject(requestStr, AbstractMsTestElement.class);
        // 手动校验参数
        ServiceUtils.validateParam(msTestElement);
        return requestStr;
    }

    private static void updateApiDefinition(ImportRequest request, List<ApiDefinitionDetail> updateRequestData, ApiDefinitionMapper apiMapper, ApiDefinitionBlobMapper apiBlobMapper, SqlSession sqlSession) {
        SubListUtils.dealForSubList(updateRequestData, 100, list -> {
            list.forEach(t -> {
                ApiDefinition apiDefinition = new ApiDefinition();
                apiDefinition.setId(t.getId());
                apiDefinition.setUpdateUser(request.getUserId());
                apiDefinition.setUpdateTime(System.currentTimeMillis());
                apiDefinition.setModuleId(t.getModuleId());
                apiMapper.updateByPrimaryKeySelective(apiDefinition);
                //更新blob数据
                ApiDefinitionBlob apiDefinitionBlob = new ApiDefinitionBlob();
                apiDefinitionBlob.setId(t.getId());
                apiDefinitionBlob.setRequest(JSON.toJSONBytes(t.getRequest()));
                apiDefinitionBlob.setResponse(JSON.toJSONBytes(t.getResponse()));
                apiBlobMapper.updateByPrimaryKeySelective(apiDefinitionBlob);
            });
            sqlSession.flushStatements();
        });
    }

    private static void updateApiTestCase(ImportRequest request, List<ApiTestCaseDTO> updateApiTestCaseData, ApiTestCaseMapper apiTestCaseMapper, ApiTestCaseBlobMapper apiTestCaseBlobMapper, SqlSession sqlSession) {
        SubListUtils.dealForSubList(updateApiTestCaseData, 100, list -> {
            list.forEach(t -> {
                ApiTestCase apiTestCase = new ApiTestCase();
                apiTestCase.setId(t.getId());
                apiTestCase.setUpdateUser(request.getUserId());
                apiTestCase.setUpdateTime(System.currentTimeMillis());
                apiTestCaseMapper.updateByPrimaryKeySelective(apiTestCase);
                //更新blob数据
                ApiTestCaseBlob apiDefinitionBlob = new ApiTestCaseBlob();
                apiDefinitionBlob.setId(t.getId());
                apiDefinitionBlob.setRequest(JSON.toJSONBytes(t.getRequest()));
                apiTestCaseBlobMapper.updateByPrimaryKeySelective(apiDefinitionBlob);
            });
            sqlSession.flushStatements();
        });
    }

    private static void updateApiMock(ImportRequest request, List<ApiDefinitionMockDTO> updateApiTestCaseData, ApiDefinitionMockMapper batchMockMapper, ApiDefinitionMockConfigMapper batchMockConfigMapper, SqlSession sqlSession) {
        SubListUtils.dealForSubList(updateApiTestCaseData, 100, list -> {
            list.forEach(t -> {
                ApiDefinitionMock apiMock = new ApiDefinitionMock();
                apiMock.setId(t.getId());
                apiMock.setUpdateUser(request.getUserId());
                apiMock.setUpdateTime(System.currentTimeMillis());
                batchMockMapper.updateByPrimaryKeySelective(apiMock);
                //更新blob数据
                ApiDefinitionMockConfig mockConfig = new ApiDefinitionMockConfig();
                mockConfig.setId(apiMock.getId());
                mockConfig.setMatching(JSON.toJSONString(t.getMockMatchRule()).getBytes());
                mockConfig.setResponse(JSON.toJSONString(t.getResponse()).getBytes());
                batchMockConfigMapper.updateByPrimaryKeySelective(mockConfig);
            });
            sqlSession.flushStatements();
        });
    }

    private static void updateApiDefinitionModule(ImportRequest request, List<ApiDefinitionDetail> updateModuleData, ApiDefinitionMapper apiMapper, SqlSession sqlSession) {
        SubListUtils.dealForSubList(updateModuleData, 100, list -> {
            list.forEach(t -> {
                ApiDefinition apiDefinition = new ApiDefinition();
                apiDefinition.setId(t.getId());
                apiDefinition.setModuleId(t.getModuleId());
                apiDefinition.setUpdateUser(request.getUserId());
                apiDefinition.setUpdateTime(System.currentTimeMillis());
                apiMapper.updateByPrimaryKeySelective(apiDefinition);
            });
            sqlSession.flushStatements();
        });
    }

    private void insertModule(ImportRequest request, List<BaseTreeNode> addModuleList, ApiDefinitionModuleMapper moduleMapper, SqlSession sqlSession) {
        SubListUtils.dealForSubList(addModuleList, 100, list -> {
            list.forEach(t -> {
                ApiDefinitionModule module = new ApiDefinitionModule();
                module.setId(t.getId());
                module.setName(t.getName());
                module.setParentId(t.getParentId());
                module.setProjectId(request.getProjectId());
                module.setCreateUser(request.getUserId());
                module.setPos(getImportNextModuleOrder(request.getProjectId()));
                module.setCreateTime(System.currentTimeMillis());
                module.setUpdateUser(request.getUserId());
                module.setUpdateTime(System.currentTimeMillis());
                moduleMapper.insertSelective(module);
            });
            sqlSession.flushStatements();
        });
    }

    /**
     * 预导入数据分析   根据请求配置，判断哪些数据新增、哪些数据修改且修改它的哪部分数据、
     */
    private ApiDefinitionPreImportAnalysisResult preImportAnalysis(ImportRequest request, ApiImportDataAnalysisResult insertAndUpdateData) {
        ApiDefinitionPreImportAnalysisResult preImportAnalysisResult = new ApiDefinitionPreImportAnalysisResult();

        // api模块树查询
        List<BaseTreeNode> apiModules = this.buildTreeData(request.getProjectId(), request.getProtocol());
        Map<String, BaseTreeNode> modulePathMap = apiModules.stream().collect(Collectors.toMap(BaseTreeNode::getPath, t -> t));
        // 新增数据处理
        this.inertDataAnalysis(preImportAnalysisResult, request, modulePathMap, insertAndUpdateData);

        // 已有数据处理
        this.existenceDataAnalysis(preImportAnalysisResult, request, modulePathMap, insertAndUpdateData);

        return preImportAnalysisResult;
    }

    /*
        新增数据准备
        指定了导入模块： 直接塞入指定模块中。
        未指定导入模块： 接口有模块，就放在那个模块下。  接口没模块就放在未规划模块内
     */
    private void inertDataAnalysis(ApiDefinitionPreImportAnalysisResult apiDefinitionPreImportAnalysisResult, ImportRequest request, Map<String, BaseTreeNode> modulePathMap, ApiImportDataAnalysisResult analysisResult) {
        for (ApiDefinitionDetail apiData : analysisResult.getInsertApiList()) {
            if (StringUtils.isNotEmpty(request.getModuleId())) {
                // 指定了导入模块： 直接塞入指定模块中。
                apiData.setModuleId(request.getModuleId());
            } else {
                // 未指定导入模块： 接口有模块，就放在那个模块下。  接口没模块就放在未规划模块内
                if (StringUtils.isEmpty(apiData.getModulePath())) {
                    apiData.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
                } else {
                    String modulePath = apiData.getModulePath();
                    if (!StringUtils.startsWith(modulePath, "/")) {
                        modulePath = "/" + modulePath;
                        apiData.setModulePath(modulePath);
                    }
                    if (!modulePathMap.containsKey(modulePath)) {
                        apiDefinitionPreImportAnalysisResult.getInsertModuleList().addAll(TreeNodeParseUtils.getInsertNodeByPath(modulePathMap, modulePath));
                    }
                    apiData.setModuleId(modulePathMap.get(modulePath).getId());
                }
            }
            apiDefinitionPreImportAnalysisResult.getLogData().put(apiData.getId(), apiData);
            apiDefinitionPreImportAnalysisResult.getInsertApiData().add(apiData);
            //判断是否更新用例
            if (request.isSyncCase()) {
                if (analysisResult.getApiIdAndTestCaseMap().containsKey(apiData.getId())) {
                    apiDefinitionPreImportAnalysisResult.getInsertApiCaseList().addAll(analysisResult.getApiIdAndTestCaseMap().get(apiData.getId()));
                }
            }
            //判断是否更新Mock
            if (request.isSyncMock()) {
                if (analysisResult.getApiIdAndMockMap().containsKey(apiData.getId())) {
                    apiDefinitionPreImportAnalysisResult.getInsertApiMockList().addAll(analysisResult.getApiIdAndMockMap().get(apiData.getId()));
                }
            }
        }
    }

    // 已有数据处理
    private void existenceDataAnalysis(ApiDefinitionPreImportAnalysisResult apiDefinitionPreImportAnalysisResult, ImportRequest request, Map<String, BaseTreeNode> modulePathMap, ApiImportDataAnalysisResult analysisResult) {
        //不选择覆盖接口或者数据为空：终止操作
        if (CollectionUtils.isEmpty(analysisResult.getExistenceApiList()) || !request.isCoverData()) {
            return;
        }
        List<ExistenceApiDefinitionDetail> existenceApiList = analysisResult.getExistenceApiList();
        List<String> existenceApiIdList = new ArrayList<>();
        existenceApiList.forEach(item -> existenceApiIdList.add(item.getExistenceApiDefinition().getId()));
        Map<String, ApiDefinitionBlob> existenceApiDefinitionBlobMap = this.selectApiDefinitionBlobIdMap(existenceApiIdList);
        Map<String, List<ApiTestCase>> existenceApiTestCaseMap = this.selectApiTestCaseIdMap(existenceApiIdList);
        Map<String, List<ApiDefinitionMock>> existenceApiMockMap = this.selectApiMockIdMap(existenceApiIdList);

        for (ExistenceApiDefinitionDetail existenceApiDefinitionDetail : existenceApiList) {
            ApiDefinitionDetail importApi = existenceApiDefinitionDetail.getImportApiDefinition();
            ApiDefinitionDetail existenceApi = existenceApiDefinitionDetail.getExistenceApiDefinition();

            boolean isSameRequest = false;
            ApiDefinitionBlob apiDefinitionBlob = existenceApiDefinitionBlobMap.get(existenceApi.getId());
            MsHTTPElement existenceMsHTTPElement = null;
            if (apiDefinitionBlob != null) {
                existenceMsHTTPElement = ApiDataUtils.parseObject(new String(apiDefinitionBlob.getRequest()), MsHTTPElement.class);
                //判断参数是否一样  参数类型有 请求头  请求参数  请求体
                isSameRequest = dataIsSame(existenceMsHTTPElement, (MsHTTPElement) importApi.getRequest());
            }

            String importApiId = importApi.getId();
            importApi.setId(existenceApi.getId());
            if (request.isCoverModule()) {
                // 未指定导入模块： 接口有模块，就放在那个模块下。  接口没模块就放在未规划模块内
                String modulePath = importApi.getModulePath();
                if (modulePathMap.containsKey(modulePath)) {
                    importApi.setModuleId(modulePathMap.get(modulePath).getId());
                }
                boolean isSameModule = StringUtils.equals(importApi.getModuleId(), existenceApi.getModuleId());
                /*
                    开启模块覆盖并覆盖接口，此时有4种情况：
                 */
                if (isSameRequest && isSameModule) {
                    //接口请求一样，模块一样： 不处理
                    continue;
                }

                if (!isSameRequest && isSameModule) {
                    //接口请求不一样，模块一样：更新接口的非模块信息
                    this.updateApiDefinitionRequest(importApi, existenceMsHTTPElement, request.getPlatform());
                    apiDefinitionPreImportAnalysisResult.getUpdateApiData().add(importApi);
                } else if (isSameRequest) {
                    //接口请求一样，模块不一样：只更新接口模块信息
                    importApi.setModuleId(existenceApi.getModuleId());
                    this.updateApiDefinitionModule(importApi, modulePathMap, request.getModuleId(), apiDefinitionPreImportAnalysisResult);
                    apiDefinitionPreImportAnalysisResult.getUpdateModuleApiList().add(importApi);
                } else {
                    //接口请求不一样，模块不一样：更新接口所有信息
                    this.updateApiDefinitionRequest(importApi, existenceMsHTTPElement, request.getPlatform());
                    this.updateApiDefinitionModule(importApi, modulePathMap, request.getModuleId(), apiDefinitionPreImportAnalysisResult);
                    apiDefinitionPreImportAnalysisResult.getUpdateApiData().add(importApi);
                }
                apiDefinitionPreImportAnalysisResult.getLogData().put(importApi.getId(), importApi);


            } else {
                if (!isSameRequest) {
                    //覆盖接口、不覆盖模块、 接口请求不一样，要更新接口的非模块信息;接口请求一样不处理，
                    this.updateApiDefinitionRequest(importApi, existenceMsHTTPElement, request.getPlatform());
                    apiDefinitionPreImportAnalysisResult.getLogData().put(importApi.getId(), importApi);
                    apiDefinitionPreImportAnalysisResult.getUpdateApiData().add(importApi);
                }
            }
            //是否同步用例
            if (request.isSyncCase()) {
                boolean hasSyncCase = this.existenceApiTestCasePreparation(apiDefinitionPreImportAnalysisResult,
                        analysisResult.getApiIdAndTestCaseMap().get(importApiId),
                        importApi.getId(),
                        existenceApiTestCaseMap.get(importApi.getId()));
                if (hasSyncCase && !apiDefinitionPreImportAnalysisResult.getLogData().containsKey(importApi.getId())) {
                    apiDefinitionPreImportAnalysisResult.getLogData().put(importApi.getId(), importApi);
                }
            }
            //是否同步Mock
            if (request.isSyncMock()) {
                boolean hasSyncCase = this.existenceApiMockPreparation(apiDefinitionPreImportAnalysisResult,
                        analysisResult.getApiIdAndMockMap().get(importApiId),
                        importApi.getId(),
                        existenceApiMockMap.get(importApi.getId()));
                if (hasSyncCase && !apiDefinitionPreImportAnalysisResult.getLogData().containsKey(importApi.getId())) {
                    apiDefinitionPreImportAnalysisResult.getLogData().put(importApi.getId(), importApi);
                }
            }
        }
    }

    /**
     * 覆盖接口的选项下，并且导入接口模块和已有接口模块不一致时，更新接口模块规则如下：
     * 1.导入时指定了导入模块：
     * a).导入的接口没有模块，就放在导入指定模块下
     * b).导入的接口有模块，在导入指定模块下select-or-create模块
     * c).导入时指定的是未规划模块，则所有的接口都放在未规划模块下
     * 2.导入时未指定模块
     * a).导入的接口没有模块，放在未规划模块下。
     * b).导入的接口有模块，根据导入接口的模块路径，从根目录select-or-create模块
     *
     * @param importApi                            要导入的api
     * @param modulePathMap                        已有的模块路径map
     * @param importModuleId                       导入指定的模块
     * @param apiDefinitionPreImportAnalysisResult 如果有需要创建的模块，要存储在result中
     */
    private void updateApiDefinitionModule(ApiDefinitionDetail importApi, Map<String, BaseTreeNode> modulePathMap, String importModuleId, ApiDefinitionPreImportAnalysisResult apiDefinitionPreImportAnalysisResult) {
        if (StringUtils.equalsIgnoreCase(importModuleId, ModuleConstants.DEFAULT_NODE_ID)) {
            importApi.setModuleId(ModuleConstants.DEFAULT_NODE_ID);
            return;
        }

        if (StringUtils.isNotBlank(importApi.getModulePath())) {
            // 只要导入的接口有模块，就要通过1b 或者 2b 来进行判断
            BaseTreeNode selectTreeNode = null;
            if (StringUtils.isNotBlank(importModuleId)) {
                selectTreeNode = modulePathMap.values().stream().filter(t -> StringUtils.equals(t.getId(), importModuleId)).findFirst().orElse(null);
            }
            String selectModulePath = selectTreeNode == null ? StringUtils.EMPTY : selectTreeNode.getPath();
            if (!StringUtils.startsWith(importApi.getModulePath(), "/")) {
                importApi.setModulePath("/" + importApi.getModulePath());
            }
            String fullPath = TreeNodeParseUtils.genFullModulePath(selectModulePath, importApi.getModulePath());
            if (!modulePathMap.containsKey(fullPath)) {
                apiDefinitionPreImportAnalysisResult.getInsertModuleList().addAll(TreeNodeParseUtils.getInsertNodeByPath(modulePathMap, fullPath));
            }
            importApi.setModuleId(modulePathMap.get(fullPath).getId());
        } else {
            //导入的接口没模块，就要放在指定的导入模块 或者 根目录 下
            importApi.setModuleId(StringUtils.isBlank(importModuleId) ? ModuleConstants.DEFAULT_NODE_ID : importModuleId);
        }
    }

    private void updateApiDefinitionRequest(ApiDefinitionDetail importApi, MsHTTPElement existenceMsHTTPElement, String platform) {
        if (StringUtils.equals(platform, ApiImportPlatform.Swagger3.name()) && existenceMsHTTPElement != null) {
            //swagger如果接口已存在，并且要覆盖， 那么是不能覆盖原来接口的前后置数据的（因为swagger文件里没有）
            importApi.getRequest().setChildren(existenceMsHTTPElement.getChildren());
        }
    }

    public Map<String, ApiDefinitionBlob> selectApiDefinitionBlobIdMap(List<String> apiDefinitionIds) {
        ApiDefinitionBlobExample example = new ApiDefinitionBlobExample();
        example.createCriteria().andIdIn(apiDefinitionIds);
        List<ApiDefinitionBlob> apiDefinitionBlobs = apiDefinitionBlobMapper.selectByExampleWithBLOBs(example);
        return apiDefinitionBlobs.stream().collect(Collectors.toMap(ApiDefinitionBlob::getId, t -> t));

    }

    public Map<String, List<ApiTestCase>> selectApiTestCaseIdMap(List<String> apiDefinitionIds) {
        ApiTestCaseExample example = new ApiTestCaseExample();
        example.createCriteria().andApiDefinitionIdIn(apiDefinitionIds);
        List<ApiTestCase> apiTestCaseList = apiTestCaseMapper.selectByExample(example);
        return apiTestCaseList.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
    }

    public Map<String, List<ApiDefinitionMock>> selectApiMockIdMap(List<String> apiDefinitionIds) {
        ApiDefinitionMockExample example = new ApiDefinitionMockExample();
        example.createCriteria().andApiDefinitionIdIn(apiDefinitionIds);
        List<ApiDefinitionMock> apiMockList = apiDefinitionMockMapper.selectByExample(example);
        return apiMockList.stream().collect(Collectors.groupingBy(ApiDefinitionMock::getApiDefinitionId));
    }

    /**
     * 已有接口的用例数据处理:通过用例名判断唯一性。名称存在便更新，名称不存在便新增
     *
     * @param apiDefinitionPreImportAnalysisResult 导入数据预处理结果集
     * @param importApiCaseList              要导入的api用例数据
     * @param existenceApiId                 已存在的apiId
     * @param existenceApiTestCaseList       已存在的接口用例
     * @return 是否存在新增或更新的用例数据
     */
    private boolean existenceApiTestCasePreparation(ApiDefinitionPreImportAnalysisResult apiDefinitionPreImportAnalysisResult, List<ApiTestCaseDTO> importApiCaseList, String existenceApiId, List<ApiTestCase> existenceApiTestCaseList) {
        if (CollectionUtils.isNotEmpty(importApiCaseList)) {
            Map<String, ApiTestCase> apiTestCaseNameMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(existenceApiTestCaseList)) {
                apiTestCaseNameMap = existenceApiTestCaseList.stream().collect(Collectors.toMap(ApiTestCase::getName, t -> t));
            }
            List<ApiTestCaseDTO> insertList = new ArrayList<>();
            List<ApiTestCaseDTO> updateList = new ArrayList<>();
            for (ApiTestCaseDTO apiTestCaseDTO : importApiCaseList) {
                apiTestCaseDTO.setApiDefinitionId(existenceApiId);
                // 通过名字判断唯一性。名称存在便更新，名称不存在便新增
                if (apiTestCaseNameMap.containsKey(apiTestCaseDTO.getName())) {
                    apiTestCaseDTO.setId(apiTestCaseNameMap.get(apiTestCaseDTO.getName()).getId());
                    updateList.add(apiTestCaseDTO);
                } else {
                    insertList.add(apiTestCaseDTO);
                }
            }
            if (CollectionUtils.isNotEmpty(insertList) || CollectionUtils.isNotEmpty(updateList)) {
                apiDefinitionPreImportAnalysisResult.getUpdateApiCaseList().addAll(updateList);
                apiDefinitionPreImportAnalysisResult.getInsertApiCaseList().addAll(insertList);
                return true;
            }
        }
        return false;
    }

    /**
     * 已有接口的Mock数据处理:通过Mock名判断唯一性。名称存在便更新，名称不存在便新增
     *
     * @param apiDefinitionPreImportAnalysisResult 导入数据预处理结果集
     * @param apiMockDTOList                       要导入的api用例数据
     * @param existenceApiId                       已存在的apiId
     * @param existenceApiMockList                 已存在的接口用例
     * @return 是否存在新增或更新的用例数据
     */
    private boolean existenceApiMockPreparation(ApiDefinitionPreImportAnalysisResult apiDefinitionPreImportAnalysisResult, List<ApiDefinitionMockDTO> apiMockDTOList, String existenceApiId, List<ApiDefinitionMock> existenceApiMockList) {
        if (CollectionUtils.isNotEmpty(apiMockDTOList)) {
            Map<String, ApiDefinitionMock> apiMockNameMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(existenceApiMockList)) {
                apiMockNameMap = existenceApiMockList.stream().collect(Collectors.toMap(ApiDefinitionMock::getName, t -> t));
            }
            List<ApiDefinitionMockDTO> insertList = new ArrayList<>();
            List<ApiDefinitionMockDTO> updateList = new ArrayList<>();
            for (ApiDefinitionMockDTO apiMockDTO : apiMockDTOList) {
                apiMockDTO.setApiDefinitionId(existenceApiId);
                // 通过名字判断唯一性。名称存在便更新，名称不存在便新增
                if (apiMockNameMap.containsKey(apiMockDTO.getName())) {
                    apiMockDTO.setId(apiMockNameMap.get(apiMockDTO.getName()).getId());
                    updateList.add(apiMockDTO);
                } else {
                    insertList.add(apiMockDTO);
                }
            }
            if (CollectionUtils.isNotEmpty(insertList) || CollectionUtils.isNotEmpty(updateList)) {
                apiDefinitionPreImportAnalysisResult.getUpdateApiMockList().addAll(updateList);
                apiDefinitionPreImportAnalysisResult.getInsertApiMockList().addAll(insertList);
                return true;
            }
        }
        return false;
    }

    public boolean dataIsSame(MsHTTPElement dbRequest, MsHTTPElement importRequest) {
        // 判断请求头是否一样
        if (headersHasDifferent(dbRequest.getHeaders(), importRequest.getHeaders())) {
            return false;
        }

        // 判断请求参数是否一样
        if (paramsAreSame(dbRequest.getQuery(), importRequest.getQuery()) ||
                paramsAreSame(dbRequest.getRest(), importRequest.getRest())) {
            return false;
        }

        // 判断请求体是否一样
        return bodyAreSame(dbRequest.getBody(), importRequest.getBody());
    }

    private boolean headersHasDifferent(List<MsHeader> dbHeaders, List<MsHeader> importHeaders) {
        if (CollectionUtils.isEmpty(dbHeaders) && CollectionUtils.isEmpty(importHeaders)) {
            return false;
        }

        List<String> dbHeaderKeys = dbHeaders.stream().map(MsHeader::getKey).toList();
        List<String> importHeaderKeys = importHeaders.stream().map(MsHeader::getKey).toList();

        return keysHasDifferent(dbHeaderKeys, importHeaderKeys);
    }

    private boolean paramsAreSame(List<? extends KeyValueEnableParam> dbParams, List<? extends KeyValueEnableParam> importParams) {
        if (CollectionUtils.isEmpty(dbParams) && CollectionUtils.isEmpty(importParams)) {
            return false;
        }

        List<String> dbParamKeys = dbParams.stream().map(KeyValueEnableParam::getKey).toList();
        List<String> importParamKeys = importParams.stream().map(KeyValueEnableParam::getKey).toList();

        return keysHasDifferent(dbParamKeys, importParamKeys);
    }

    private boolean bodyAreSame(Body dbBody, Body importBody) {
        if (dbBody == null && importBody == null) {
            return true;
        }

        if (dbBody != null && importBody != null) {
            if (!StringUtils.equals(dbBody.getBodyType(), importBody.getBodyType())) {
                return false;
            }

            return switch (Body.BodyType.valueOf(dbBody.getBodyType())) {
                case FORM_DATA ->
                        !formBodyHaDifferent(dbBody.getFormDataBody(), importBody.getFormDataBody());
                case WWW_FORM ->
                        !wwwFormBodyHasDifferent(dbBody.getWwwFormBody(), importBody.getWwwFormBody());
                case RAW -> rawBodyAreSame(dbBody.getRawBody(), importBody.getRawBody());
                case JSON -> jsonBodyAreSame(dbBody.getJsonBody(), importBody.getJsonBody());
                case XML -> xmlBodyAreSame(dbBody.getXmlBody(), importBody.getXmlBody());
                default -> true;
            };
        }

        return true;
    }

    private boolean xmlBodyAreSame(XmlBody xmlBody, XmlBody importXmlBody) {
        if (xmlBody == null && importXmlBody == null) {
            return true;
        }
        return xmlBody != null && importXmlBody != null && StringUtils.equals(xmlBody.getValue(), importXmlBody.getValue());

    }

    private boolean formBodyHaDifferent(FormDataBody dbFormBody, FormDataBody importFormBody) {
        if (dbFormBody == null && importFormBody == null) {
            return false;
        }

        if (dbFormBody != null && importFormBody != null) {
            List<FormDataKV> dbFormValues = dbFormBody.getFormValues();
            List<FormDataKV> importFormValues = importFormBody.getFormValues();

            return keysHasDifferent(
                    dbFormValues.stream().map(FormDataKV::getKey).toList(),
                    importFormValues.stream().map(FormDataKV::getKey).toList()
            );
        }

        return false;
    }

    private boolean wwwFormBodyHasDifferent(WWWFormBody dbWwwBody, WWWFormBody importWwwBody) {
        if (dbWwwBody == null && importWwwBody == null) {
            return false;
        }

        if (dbWwwBody != null && importWwwBody != null) {
            List<WWWFormKV> dbWwwValues = dbWwwBody.getFormValues();
            List<WWWFormKV> importWwwValues = importWwwBody.getFormValues();

            return keysHasDifferent(
                    dbWwwValues.stream().map(WWWFormKV::getKey).toList(),
                    importWwwValues.stream().map(WWWFormKV::getKey).toList()
            );
        }

        return false;
    }

    private boolean rawBodyAreSame(RawBody dbRawBody, RawBody importRawBody) {
        if (dbRawBody == null && importRawBody == null) {
            return true;
        }
        return dbRawBody != null && importRawBody != null && StringUtils.equals(dbRawBody.getValue(), importRawBody.getValue());
    }

    private boolean jsonBodyAreSame(JsonBody dbJsonBody, JsonBody importJsonBody) {
        if (dbJsonBody == null && importJsonBody == null) {
            return true;
        }

        if (dbJsonBody != null && importJsonBody != null) {
            if (!StringUtils.equals(dbJsonBody.getJsonValue(), importJsonBody.getJsonValue())) {
                return false;
            }

            JsonSchemaItem dbJsonSchema = dbJsonBody.getJsonSchema();
            JsonSchemaItem importJsonSchema = importJsonBody.getJsonSchema();

            return jsonSchemasAreSame(dbJsonSchema, importJsonSchema);
        }

        return true;
    }

    private boolean jsonSchemasAreSame(JsonSchemaItem dbJsonSchema, JsonSchemaItem importJsonSchema) {
        if (dbJsonSchema == null && importJsonSchema == null) {
            return true;
        }

        if (dbJsonSchema != null && importJsonSchema != null) {
            return jsonSchemaIsSame(dbJsonSchema, importJsonSchema);
        }

        return true;
    }

    //判断jsonschema的参数是否一样

    /**
     * 判断jsonschema的参数是否一样
     *
     * @param jsonSchema       数据库中的数据
     * @param importJsonSchema 导入的生成的jsonschema
     * @return true 一样 false 不一样 一样的话就不需要更新
     */
    private static boolean jsonSchemaIsSame(JsonSchemaItem jsonSchema, JsonSchemaItem importJsonSchema) {
        boolean same = true;
        if (jsonSchema == null && importJsonSchema == null) {
            return true;
        }
        if (jsonSchema != null && !StringUtils.equals(jsonSchema.getType(), importJsonSchema.getType())) {
            return false;
        }
        if (jsonSchema != null && StringUtils.equals(jsonSchema.getType(), PropertyConstant.OBJECT)) {
            Map<String, JsonSchemaItem> properties = jsonSchema.getProperties();
            Map<String, JsonSchemaItem> importProperties = importJsonSchema.getProperties();
            if (MapUtils.isNotEmpty(properties) || MapUtils.isNotEmpty(importProperties)) {
                List<String> dbJsonKeys = properties.keySet().stream().toList();
                List<String> importJsonKeys = importProperties.keySet().stream().toList();
                if (keysHasDifferent(dbJsonKeys, importJsonKeys)) {
                    return false;
                }
                //遍历判断每个参数是否一样
                for (String key : dbJsonKeys) {
                    JsonSchemaItem jsonSchemaItem = properties.get(key);
                    JsonSchemaItem importJsonSchemaItem = importProperties.get(key);
                    if (!jsonSchemaIsSame(jsonSchemaItem, importJsonSchemaItem)) {
                        same = false;
                        break;
                    }
                }
            }
        }
        if (jsonSchema != null && StringUtils.equals(jsonSchema.getType(), PropertyConstant.ARRAY)) {
            List<JsonSchemaItem> jsonSchemaItems = jsonSchema.getItems();
            List<JsonSchemaItem> importJsonSchemaItems = importJsonSchema.getItems();
            if (jsonSchemaItems != null && importJsonSchemaItems != null) {
                if (jsonSchemaItems.size() != importJsonSchemaItems.size()) {
                    return false;
                }
                for (int i = 0; i < jsonSchemaItems.size(); i++) {
                    if (!jsonSchemaIsSame(jsonSchemaItems.get(i), importJsonSchemaItems.get(i))) {
                        return false;
                    }
                }
            }
        }
        return same;
    }

    private static boolean keysHasDifferent(List<String> dbKeys, List<String> importKeys) {
        if (CollectionUtils.isEmpty(dbKeys) && CollectionUtils.isEmpty(importKeys)) {
            return false;
        }
        if (dbKeys.size() != importKeys.size()) {
            return true;
        }
        //看看是否有差集
        List<String> differenceRest = dbKeys.stream().filter(t -> !importKeys.contains(t)).toList();
        return CollectionUtils.isNotEmpty(differenceRest);
    }

    /**
     * 构造树结构 但是这里需要module的path
     *
     * @param projectId 项目id
     * @param protocol  协议
     * @return 树结构
     */
    public List<BaseTreeNode> buildTreeData(String projectId, String protocol) {
        ApiModuleRequest request = new ApiModuleRequest();
        request.setProjectId(projectId);
        request.setProtocols(List.of(protocol));
        List<BaseTreeNode> apiModuleList = extApiDefinitionModuleMapper.selectBaseByRequest(request);
        return this.buildTreeAndCountResource(apiModuleList, true, Translator.get(UNPLANNED_API));
    }

    public List<BaseTreeNode> buildTreeAndCountResource(List<BaseTreeNode> traverseList, boolean haveVirtualRootNode, String virtualRootName) {
        List<BaseTreeNode> baseTreeNodeList = new ArrayList<>();
        if (haveVirtualRootNode) {
            BaseTreeNode defaultNode = new BaseTreeNode(ModuleConstants.DEFAULT_NODE_ID, virtualRootName, ModuleConstants.NODE_TYPE_DEFAULT, ModuleConstants.ROOT_NODE_PARENT_ID);
            defaultNode.setPath(StringUtils.join("/", defaultNode.getName()));
            baseTreeNodeList.add(defaultNode);
        }
        int lastSize = 0;
        Map<String, BaseTreeNode> baseTreeNodeMap = new HashMap<>();
        while (CollectionUtils.isNotEmpty(traverseList) && traverseList.size() != lastSize) {
            lastSize = traverseList.size();
            List<BaseTreeNode> notMatchedList = new ArrayList<>();
            for (BaseTreeNode treeNode : traverseList) {
                if (!baseTreeNodeMap.containsKey(treeNode.getParentId()) && !StringUtils.equalsIgnoreCase(treeNode.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
                    notMatchedList.add(treeNode);
                    continue;
                }
                BaseTreeNode node = new BaseTreeNode(treeNode.getId(), treeNode.getName(), treeNode.getType(), treeNode.getParentId());
                node.genModulePath(baseTreeNodeMap.get(treeNode.getParentId()));
                baseTreeNodeMap.put(treeNode.getId(), node);

                baseTreeNodeList.add(node);
            }
            traverseList = notMatchedList;

            //            ApiDefinitionModule module = apiDefinitionModuleMapper.selectByPrimaryKey("ABC");
        }
        return baseTreeNodeList;
    }

}
