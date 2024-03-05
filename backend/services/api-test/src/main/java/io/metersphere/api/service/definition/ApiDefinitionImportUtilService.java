package io.metersphere.api.service.definition;

import io.metersphere.api.constants.ApiDefinitionStatus;
import io.metersphere.api.constants.ApiImportPlatform;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionBlob;
import io.metersphere.api.domain.ApiDefinitionBlobExample;
import io.metersphere.api.domain.ApiDefinitionModule;
import io.metersphere.api.dto.converter.ApiDefinitionImport;
import io.metersphere.api.dto.converter.ApiDefinitionImportDetail;
import io.metersphere.api.dto.converter.ApiDetailWithData;
import io.metersphere.api.dto.converter.ApiDetailWithDataUpdate;
import io.metersphere.api.dto.definition.ApiDefinitionDTO;
import io.metersphere.api.dto.definition.ApiDefinitionPageRequest;
import io.metersphere.api.dto.definition.ApiModuleRequest;
import io.metersphere.api.dto.request.ImportRequest;
import io.metersphere.api.dto.request.http.Header;
import io.metersphere.api.dto.request.http.MsHTTPElement;
import io.metersphere.api.dto.request.http.QueryParam;
import io.metersphere.api.dto.request.http.RestParam;
import io.metersphere.api.dto.request.http.body.*;
import io.metersphere.api.dto.schema.JsonSchemaItem;
import io.metersphere.api.mapper.*;
import io.metersphere.api.utils.ApiDataUtils;
import io.metersphere.project.constants.PropertyConstant;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ExtBaseProjectVersionMapper;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.constants.ApplicationNumScope;
import io.metersphere.sdk.constants.HttpMethodConstants;
import io.metersphere.sdk.constants.ModuleConstants;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.dto.sdk.ApiDefinitionCaseDTO;
import io.metersphere.system.dto.sdk.BaseTreeNode;
import io.metersphere.system.dto.sdk.SessionUser;
import io.metersphere.system.log.constants.OperationLogModule;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.log.dto.LogDTO;
import io.metersphere.system.log.service.OperationLogService;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.sender.AfterReturningNoticeSendService;
import io.metersphere.system.uid.IDGenerator;
import io.metersphere.system.uid.NumGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiDefinitionImportUtilService {

    private static final String UNPLANNED_API = "api_unplanned_request";
    public static final Long ORDER_STEP = 5000L;
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
    private ApiDefinitionModuleService apiDefinitionModuleService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private OperationLogService operationLogService;
    @Resource
    private AfterReturningNoticeSendService afterReturningNoticeSendService;

    private static final String FILE_JMX = "jmx";
    private static final String FILE_HAR = "har";
    private static final String FILE_JSON = "json";

    public void checkFileSuffixName(ImportRequest request, String suffixName) {
        if (FILE_JMX.equalsIgnoreCase(suffixName)) {
            if (!ApiImportPlatform.Jmeter.name().equalsIgnoreCase(request.getPlatform())) {
                throw new MSException(Translator.get("file_format_does_not_meet_requirements"));
            }
        }
        if (FILE_HAR.equalsIgnoreCase(suffixName)) {
            if (!ApiImportPlatform.Har.name().equalsIgnoreCase(request.getPlatform())) {
                throw new MSException(Translator.get("file_format_does_not_meet_requirements"));
            }
        }
        if (FILE_JSON.equalsIgnoreCase(suffixName)) {
            if (ApiImportPlatform.Har.name().equalsIgnoreCase(request.getPlatform()) || ApiImportPlatform.Jmeter.name().equalsIgnoreCase(request.getPlatform())) {
                throw new MSException(Translator.get("file_format_does_not_meet_requirements"));
            }
        }
    }

    public void importApi(ImportRequest request, ApiDefinitionImport apiImport, SessionUser user) {
        String defaultVersion = extBaseProjectVersionMapper.getDefaultVersion(request.getProjectId());
        request.setDefaultVersion(defaultVersion);
        if (request.getVersionId() == null) {
            request.setVersionId(defaultVersion);
        }
        List<ApiDefinitionImportDetail> initData = apiImport.getData();

        //TODO 查询项目菜单参数
        /*ProjectApplicationExample applicationExample = new ProjectApplicationExample();
        applicationExample.createCriteria().andProjectIdEqualTo(request.getProjectId()).andTypeEqualTo("API_URL_REPEATABLE");
        List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(applicationExample);
        if (CollectionUtils.isNotEmpty(projectApplications)) {
            String typeValue = projectApplications.get(0).getTypeValue();
        }*/
        //过滤(一次只导入一个协议)
        List<ApiDefinitionImportDetail> filterData = initData.stream().filter(t -> t.getProtocol().equals(request.getProtocol())).collect(Collectors.toList());
        if (filterData.isEmpty()) {
            return;
        }

        //处理数据，判断数据是否重复
        dealWithData(request, filterData, user);

    }

    private void dealWithData(ImportRequest request, List<ApiDefinitionImportDetail> importData, SessionUser user) {
        //查询数据库中所有的数据， 用于判断是否重复
        ApiDefinitionPageRequest pageRequest = new ApiDefinitionPageRequest();
        pageRequest.setProjectId(request.getProjectId());
        pageRequest.setProtocol(request.getProtocol());
        //TODO 如果是有版本的话 需要加上版本的判断
        List<ApiDefinitionImportDetail> apiLists = extApiDefinitionMapper.importList(pageRequest);
        List<BaseTreeNode> apiModules = this.buildTreeData(request.getProjectId(), request.getProtocol());
        //将apiModules转换成新的map 要求key是attachInfo中的modulePath 使用stream实现
        Map<String, BaseTreeNode> modulePathMap = apiModules.stream().collect(Collectors.toMap(BaseTreeNode::getPath, t -> t));
        Map<String, BaseTreeNode> idModuleMap = apiModules.stream().collect(Collectors.toMap(BaseTreeNode::getId, apiModuleDTO -> apiModuleDTO));
        //如果导入的时候选择了模块，需要把所有的导入数据的模块路径前面拼接上选择的模块路径
        if (StringUtils.isNotBlank(request.getModuleId())) {
            BaseTreeNode baseTreeNode = idModuleMap.get(request.getModuleId());
            String modulePath = baseTreeNode.getPath();
            importData.forEach(t -> {
                t.setModulePath(modulePath + t.getModulePath());
            });
        }
        //去掉apiLists中不存在的模块数据
        apiLists = apiLists.stream().filter(t -> modulePathMap.containsKey(t.getModulePath())).toList();
        apiLists.forEach(t -> {
            t.setModulePath(idModuleMap.get(t.getModuleId()) != null ? idModuleMap.get(t.getModuleId()).getPath() : StringUtils.EMPTY);
        });
        ApiDetailWithData apiDealWithData = new ApiDetailWithData();
        //判断数据是否是唯一的
        checkApiDataOnly(request, importData, apiLists, apiDealWithData);

        ApiDetailWithDataUpdate apiDetailWithDataUpdate = new ApiDetailWithDataUpdate();
        getNeedUpdateData(request, apiDealWithData, apiDetailWithDataUpdate);

        //数据入库
        insertData(modulePathMap, idModuleMap, apiDetailWithDataUpdate, request, user);

    }

    public Long getNextOrder(String projectId) {
        Long pos = extApiDefinitionMapper.getPos(projectId);
        return (pos == null ? 0 : pos) + ORDER_STEP;
    }

    public Long getImportNextOrder(String projectId) {
        Long order = currentApiOrder.get();
        if (order == null) {
            order = getNextOrder(projectId);
        }
        order = order + ORDER_STEP;
        currentApiOrder.set(order);
        return order;
    }

   /* public Long getImportNextCaseOrder(String projectId) {
        Long order = currentApiCaseOrder.get();
        if (order == null) {
            order = apiTestCaseService.getNextOrder(projectId);
        }
        order = order + ORDER_STEP;
        currentApiCaseOrder.set(order);
        return order;
    }*/

    public Long getImportNextModuleOrder(String projectId) {
        Long order = currentModuleOrder.get();
        if (order == null) {
            order = apiDefinitionModuleService.getNextOrder(projectId);
        }
        order = order + ORDER_STEP;
        currentModuleOrder.set(order);
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertData(Map<String, BaseTreeNode> modulePathMap,
                           Map<String, BaseTreeNode> idModuleMap,
                           ApiDetailWithDataUpdate apiDetailWithDataUpdate,
                           ImportRequest request, SessionUser user) {
        //先判断是否需要新增模块
        List<ApiDefinitionImportDetail> addModuleData = apiDetailWithDataUpdate.getAddModuleData();
        List<ApiDefinitionImportDetail> updateModuleData = apiDetailWithDataUpdate.getUpdateModuleData();
        //取addModuleData的模块放到set中  生成一个新的set
        Set<String> moduleSet = addModuleData.stream().map(ApiDefinitionImportDetail::getModulePath).collect(Collectors.toSet());
        //取updateModuleData的模块放到set中  生成一个新的set
        Set<String> updateModuleSet = updateModuleData.stream().map(ApiDefinitionImportDetail::getModulePath).collect(Collectors.toSet());
        moduleSet.addAll(updateModuleSet);
        //将modulePathMap的key转成set
        Set<String> modulePathSet = modulePathMap.keySet();
        //取modulePathSet中不存在的
        Set<String> differenceSet = moduleSet.stream().filter(t -> !modulePathSet.contains(t)).collect(Collectors.toSet());
        //不存在的需要新增
        List<BaseTreeNode> addModuleList = new ArrayList<>();

        currentApiCaseOrder.remove();
        currentApiOrder.remove();
        currentModuleOrder.remove();
        differenceSet.forEach(item -> {
            //解析modulePath  格式为/a/b/c
            String[] split = item.split("/");
            //一层一层的创建
            for (int i = 1; i < split.length; i++) {
                String modulePath = StringUtils.join(split, "/", 1, i + 1);
                String path = StringUtils.join("/", modulePath);
                BaseTreeNode baseTreeNode = modulePathMap.get(path);
                if (baseTreeNode == null) {
                    //创建模块
                    BaseTreeNode module = new BaseTreeNode();
                    module.setId(IDGenerator.nextStr());
                    module.setName(split[i]);
                    if (i != 1) {
                        String parentId = path.substring(0, path.lastIndexOf("/" + split[i]));
                        module.setParentId(modulePathMap.get(parentId).getId());
                    }
                    module.setPath(path);
                    addModuleList.add(module);
                    modulePathMap.put(path, module);
                    idModuleMap.put(module.getId(), module);
                }
            }
        });

        //创建模块
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ApiDefinitionModuleMapper moduleMapper = sqlSession.getMapper(ApiDefinitionModuleMapper.class);
        ApiDefinitionMapper apiMapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        ApiDefinitionBlobMapper apiBlobMapper = sqlSession.getMapper(ApiDefinitionBlobMapper.class);

        addModuleList.forEach(t -> {
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

        //取出需要更新的数据的id
        List<String> updateModuleLists = updateModuleData.stream().map(ApiDefinitionImportDetail::getId).toList();

        //更新模块数据
        updateModuleData.forEach(t -> {
            ApiDefinition apiDefinition = new ApiDefinition();
            apiDefinition.setId(t.getId());
            apiDefinition.setModuleId(modulePathMap.get(t.getModulePath()).getId());
            apiDefinition.setUpdateUser(request.getUserId());
            apiDefinition.setUpdateTime(System.currentTimeMillis());
            apiMapper.updateByPrimaryKeySelective(apiDefinition);
        });
        List<LogDTO> operationLogs = new ArrayList<>();
        List<ApiDefinitionImportDetail> updateRequestData = apiDetailWithDataUpdate.getUpdateRequestData();
        updateRequestData.forEach(t -> {
            if (CollectionUtils.isNotEmpty(updateModuleLists) && updateModuleLists.contains(t.getId())) {
                ApiDefinition apiDefinition = new ApiDefinition();
                apiDefinition.setId(t.getId());
                apiDefinition.setUpdateUser(request.getUserId());
                apiDefinition.setUpdateTime(System.currentTimeMillis());
                apiMapper.updateByPrimaryKeySelective(apiDefinition);
            }
            //更新blob数据
            ApiDefinitionBlob apiDefinitionBlob = new ApiDefinitionBlob();
            apiDefinitionBlob.setId(t.getId());
            apiDefinitionBlob.setRequest(JSON.toJSONBytes(t.getRequest()));
            apiDefinitionBlob.setResponse(JSON.toJSONBytes(t.getResponse()));
            apiBlobMapper.updateByPrimaryKeySelective(apiDefinitionBlob);
        });
        Map<String, ApiDefinitionImportDetail> logData = apiDetailWithDataUpdate.getLogData();
        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        List<ApiDefinitionCaseDTO> updateLists = new ArrayList<>();
        if (MapUtils.isNotEmpty(logData)) {
            logData.forEach((k, v) -> {
                ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
                BeanUtils.copyBean(apiDefinitionDTO, v);
                ApiDefinitionCaseDTO apiDefinitionCaseDTO = new ApiDefinitionCaseDTO();
                BeanUtils.copyBean(apiDefinitionCaseDTO, v);
                updateLists.add(apiDefinitionCaseDTO);
                LogDTO dto = new LogDTO(
                        project.getId(),
                        project.getOrganizationId(),
                        v.getId(),
                        request.getUserId(),
                        OperationLogType.IMPORT.name(),
                        OperationLogModule.API_TEST_MANAGEMENT_DEFINITION,
                        v.getName());
                dto.setHistory(true);
                dto.setPath("/api/definition/import");
                dto.setMethod(HttpMethodConstants.POST.name());
                dto.setOriginalValue(JSON.toJSONBytes(apiDefinitionDTO));
                operationLogs.add(dto);
            });
        }
        List<ApiDefinitionCaseDTO> createLists = new ArrayList<>();
        addModuleData.forEach(t -> {
            ApiDefinition apiDefinition = new ApiDefinition();
            BeanUtils.copyBean(apiDefinition, t);
            apiDefinition.setId(IDGenerator.nextStr());
            apiDefinition.setModuleId(modulePathMap.get(t.getModulePath()).getId());
            apiDefinition.setProjectId(request.getProjectId());
            apiDefinition.setProtocol(request.getProtocol());
            apiDefinition.setCreateUser(request.getUserId());
            apiDefinition.setPos(getImportNextOrder(request.getProjectId()));
            apiDefinition.setCreateTime(System.currentTimeMillis());
            apiDefinition.setUpdateUser(request.getUserId());
            apiDefinition.setUpdateTime(System.currentTimeMillis());
            apiDefinition.setNum(NumGenerator.nextNum(request.getProjectId(), ApplicationNumScope.API_DEFINITION));
            apiDefinition.setLatest(true);
            apiDefinition.setStatus(ApiDefinitionStatus.PROCESSING.name());
            apiDefinition.setRefId(apiDefinition.getId());
            apiDefinition.setVersionId(request.getVersionId());
            apiMapper.insertSelective(apiDefinition);
            //插入blob数据
            ApiDefinitionBlob apiDefinitionBlob = new ApiDefinitionBlob();
            apiDefinitionBlob.setId(apiDefinition.getId());
            apiDefinitionBlob.setRequest(JSON.toJSONBytes(t.getRequest()));
            apiDefinitionBlob.setResponse(JSON.toJSONBytes(t.getResponse()));
            apiBlobMapper.insertSelective(apiDefinitionBlob);
            ApiDefinitionDTO apiDefinitionDTO = new ApiDefinitionDTO();
            BeanUtils.copyBean(apiDefinitionDTO, t);
            LogDTO dto = new LogDTO(
                    project.getId(),
                    project.getOrganizationId(),
                    t.getId(),
                    request.getUserId(),
                    OperationLogType.IMPORT.name(),
                    OperationLogModule.API_TEST_MANAGEMENT_DEFINITION,
                    t.getName());
            dto.setHistory(true);
            dto.setPath("/api/definition/import");
            dto.setMethod(HttpMethodConstants.POST.name());
            dto.setOriginalValue(JSON.toJSONBytes(apiDefinitionDTO));

            ApiDefinitionCaseDTO apiDefinitionCaseDTO = new ApiDefinitionCaseDTO();
            BeanUtils.copyBean(apiDefinitionCaseDTO, t);
            createLists.add(apiDefinitionCaseDTO);
        });

        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        operationLogService.batchAdd(operationLogs);
        //发送通知
        List<Map> createResources = new ArrayList<>();
        createResources.addAll(JSON.parseArray(JSON.toJSONString(createLists), Map.class));
        afterReturningNoticeSendService.sendNotice(NoticeConstants.TaskType.API_DEFINITION_TASK, NoticeConstants.Event.CREATE, createResources, user, request.getProjectId());
        List<Map> updateResources = new ArrayList<>();
        updateResources.addAll(JSON.parseArray(JSON.toJSONString(updateResources), Map.class));
        afterReturningNoticeSendService.sendNotice(NoticeConstants.TaskType.API_DEFINITION_TASK, NoticeConstants.Event.UPDATE, updateResources, user, request.getProjectId());
    }

    private void getNeedUpdateData(ImportRequest request, ApiDetailWithData apiDetailWithData, ApiDetailWithDataUpdate apiDetailWithDataUpdate) {
        List<String> sameList = apiDetailWithData.getSameList();
        List<String> differenceList = apiDetailWithData.getDifferenceList();
        Map<String, ApiDefinitionImportDetail> apiDateMap = apiDetailWithData.getApiDateMap();
        Map<String, ApiDefinitionImportDetail> importDataMap = apiDetailWithData.getImportDataMap();
        List<ApiDefinitionImportDetail> updateModuleData = new ArrayList<>();
        List<ApiDefinitionImportDetail> updateRequestData = new ArrayList<>();
        List<ApiDefinitionImportDetail> addData = new ArrayList<>();
        Map<String, ApiDefinitionImportDetail> logMap = new HashMap<>();
        //判断参数是否一样  一样的参数需要判断是否需要覆盖模块  如果需要就要update数据， 如果不需要  就直接跳过
        if (CollectionUtils.isNotEmpty(sameList) && getFullCoverage(request.getCoverData())) {
            //需要覆盖数据的  会判断是否需要覆盖模块
            List<ApiDefinitionImportDetail> sameData = sameList.stream().map(apiDateMap::get).toList();
            //取所有id为新的list 需要取查询blob的数据
            List<String> sameIds = sameData.stream().map(ApiDefinitionImportDetail::getId).toList();
            ApiDefinitionBlobExample blobExample = new ApiDefinitionBlobExample();
            blobExample.createCriteria().andIdIn(sameIds);
            List<ApiDefinitionBlob> apiDefinitionBlobs = apiDefinitionBlobMapper.selectByExampleWithBLOBs(blobExample);
            Map<String, ApiDefinitionBlob> blobMap = apiDefinitionBlobs.stream().collect(Collectors.toMap(ApiDefinitionBlob::getId, t -> t));
            //判断参数是否一样
            for (ApiDefinitionImportDetail apiDefinitionDTO : sameData) {
                ApiDefinitionImportDetail importDTO = importDataMap.get(apiDefinitionDTO.getMethod() + apiDefinitionDTO.getPath());
                ApiDefinitionBlob apiDefinitionBlob = blobMap.get(apiDefinitionDTO.getId());
                if (apiDefinitionBlob != null) {
                    MsHTTPElement dbRequest = ApiDataUtils.parseObject(new String(apiDefinitionBlob.getRequest()), MsHTTPElement.class);
                    //判断参数是否一样  参数类型有 请求头  请求参数  请求体
                    boolean isSame = dataIsSame(dbRequest, (MsHTTPElement) importDTO.getRequest());
                    //判断是否开启模块覆盖
                    if (getFullCoverage(request.getCoverModule())) {
                        //判断模块是否一样
                        if (!StringUtils.equals(apiDefinitionDTO.getModulePath(), importDTO.getModulePath())) {
                            //不一样的模块需要更
                            apiDefinitionDTO.setModulePath(apiDefinitionDTO.getModulePath());
                            updateModuleData.add(apiDefinitionDTO);
                            logMap.put(apiDefinitionDTO.getId(), importDTO);
                        }
                    }
                    //不相同的数据需要覆盖  所以这里记录id就可以
                    if (!isSame) {
                        importDTO.setId(apiDefinitionDTO.getId());
                        updateRequestData.add(importDTO);
                        logMap.put(apiDefinitionDTO.getId(), importDTO);
                    }
                }
            }
        }
        //不存在的数据是肯定要插入的  TODO 这里需要判断是否需要创建模块
        if (CollectionUtils.isNotEmpty(differenceList)) {
            addData = differenceList.stream().map(importDataMap::get).toList();
        }
        apiDetailWithDataUpdate.setUpdateModuleData(updateModuleData);
        apiDetailWithDataUpdate.setUpdateRequestData(updateRequestData);
        apiDetailWithDataUpdate.setAddModuleData(addData);
        apiDetailWithDataUpdate.setLogData(logMap);
    }

    private void checkApiDataOnly(ImportRequest request,
                                  List<ApiDefinitionImportDetail> importData,
                                  List<ApiDefinitionImportDetail> apiLists,
                                  ApiDetailWithData apiDetailWithData) {
        //判断是否是同一接口 需要返回的数据 需要insert的  update的
        switch (request.getUniquelyIdentifies()) {
            case "Method & Path" -> methodAndPath(importData, apiLists, apiDetailWithData);
            default -> {
            }
        }
    }

    public void methodAndPath(List<ApiDefinitionImportDetail> importData,
                              List<ApiDefinitionImportDetail> lists,
                              ApiDetailWithData apiDetailWithData) {

        Map<String, ApiDefinitionImportDetail> apiDateMap = lists.stream().collect(Collectors.toMap(t -> t.getMethod() + t.getPath(), t -> t));
        Map<String, ApiDefinitionImportDetail> importDataMap = importData.stream().collect(Collectors.toMap(t -> t.getMethod() + t.getPath(), t -> t));
        //判断是否重复
        List<String> orgList = apiDateMap.keySet().stream().toList();
        List<String> importList = importDataMap.keySet().stream().toList();
        //取交集数据
        List<String> sameList = importList.stream().filter(orgList::contains).toList();
        // 不同接口的数据
        List<String> differenceList = importList.stream().filter(t -> !orgList.contains(t)).toList();
        apiDetailWithData.setSameList(sameList);
        apiDetailWithData.setDifferenceList(differenceList);
        apiDetailWithData.setApiDateMap(apiDateMap);
        apiDetailWithData.setImportDataMap(importDataMap);
    }

    public boolean dataIsSame(MsHTTPElement dbRequest, MsHTTPElement importRequest) {
        boolean same = true;
        //判断请求头是否一样
        List<Header> dbHeaders = dbRequest.getHeaders();
        List<Header> importHeaders = importRequest.getHeaders();
        if (CollectionUtils.isNotEmpty(dbHeaders) || CollectionUtils.isNotEmpty(importHeaders)) {
            List<String> dbHeaderKeys = dbHeaders.stream().map(Header::getKey).toList();
            List<String> importHeaderKeys = importHeaders.stream().map(Header::getKey).toList();
            if (paramsIsSame(dbHeaderKeys, importHeaderKeys)) {
                return false;
            }
        }
        //判断请求参数是否一样 query rest body
        List<QueryParam> dbQuery = dbRequest.getQuery();
        List<QueryParam> importQuery = importRequest.getQuery();
        if (CollectionUtils.isNotEmpty(dbQuery) || CollectionUtils.isNotEmpty(importQuery)) {
            List<String> dbQueryKeys = dbQuery.stream().map(QueryParam::getKey).toList();
            List<String> importQueryKeys = importQuery.stream().map(QueryParam::getKey).toList();
            if (paramsIsSame(dbQueryKeys, importQueryKeys)) {
                return false;
            }
        }
        List<RestParam> dbRest = dbRequest.getRest();
        List<RestParam> importRest = importRequest.getRest();
        if (CollectionUtils.isNotEmpty(dbRest) || CollectionUtils.isNotEmpty(importRest)) {
            List<String> dbRestKeys = dbRest.stream().map(RestParam::getKey).toList();
            List<String> importRestKeys = importRest.stream().map(RestParam::getKey).toList();
            if (paramsIsSame(dbRestKeys, importRestKeys)) {
                return false;
            }
        }
        //判断请求体是否一样
        if (dbRequest.getBody() != null || importRequest.getBody() != null) {
            //判断请求体的参数
            Body dbbody = dbRequest.getBody();
            Body importBody = importRequest.getBody();
            if (dbbody != null && importBody != null) {
                //判断null类型
                StringUtils.equals(String.valueOf(dbbody.getNoneBody()), String.valueOf(importBody.getNoneBody()));
                //判断form类型
                FormDataBody formDataBody = dbbody.getFormDataBody();
                FormDataBody importFormDataBody = importBody.getFormDataBody();
                if (ObjectUtils.isNotEmpty(formDataBody) || ObjectUtils.isNotEmpty(importFormDataBody)) {
                    List<FormDataKV> formValues = formDataBody.getFormValues();
                    List<FormDataKV> importFormValues = importFormDataBody.getFormValues();
                    if (CollectionUtils.isNotEmpty(formValues) || CollectionUtils.isNotEmpty(importFormValues)) {
                        List<String> dbFormKeys = formValues.stream().map(FormDataKV::getKey).toList();
                        List<String> importFormKeys = importFormValues.stream().map(FormDataKV::getKey).toList();
                        if (paramsIsSame(dbFormKeys, importFormKeys)) {
                            return false;
                        }
                    }
                }
                //判读www类型
                WWWFormBody wwwBody = dbbody.getWwwFormBody();
                WWWFormBody importWwwBody = importBody.getWwwFormBody();
                if (ObjectUtils.isNotEmpty(wwwBody) || ObjectUtils.isNotEmpty(importWwwBody)) {
                    List<WWWFormKV> wwwValues = wwwBody.getFormValues();
                    List<WWWFormKV> importWwwValues = importWwwBody.getFormValues();
                    if (CollectionUtils.isNotEmpty(wwwValues) || CollectionUtils.isNotEmpty(importWwwValues)) {
                        List<String> dbWwwKeys = wwwValues.stream().map(WWWFormKV::getKey).toList();
                        List<String> importWwwKeys = importWwwValues.stream().map(WWWFormKV::getKey).toList();
                        if (paramsIsSame(dbWwwKeys, importWwwKeys)) {
                            return false;
                        }
                    }
                }
                //TODO 判断binary类型

                //判断raw类型
                RawBody rawBody = dbbody.getRawBody();
                RawBody importRawBody = importBody.getRawBody();
                if (ObjectUtils.isNotEmpty(rawBody) || ObjectUtils.isNotEmpty(importRawBody)) {
                    return false;
                }
                //判断json类型
                JsonBody jsonBody = dbbody.getJsonBody();
                JsonBody importJsonBody = importBody.getJsonBody();
                if (ObjectUtils.isNotEmpty(jsonBody) || ObjectUtils.isNotEmpty(importJsonBody)) {
                    if (StringUtils.isNotBlank(jsonBody.getJsonValue()) || StringUtils.isNotBlank(importJsonBody.getJsonValue())) {
                        return false;
                    }
                    //判断jsonschema
                    JsonSchemaItem jsonSchema = jsonBody.getJsonSchema();
                    JsonSchemaItem importJsonSchema = importJsonBody.getJsonSchema();
                    if (jsonSchema != null && importJsonSchema != null) {
                        return jsonSchemaIsSame(jsonSchema, importJsonSchema);
                    }
                }
            }
        }


        return same;
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
        assert jsonSchema != null;
        if (!StringUtils.equals(jsonSchema.getType(), importJsonSchema.getType())) {
            return false;
        }
        if (StringUtils.equals(jsonSchema.getType(), PropertyConstant.OBJECT)) {
            Map<String, JsonSchemaItem> properties = jsonSchema.getProperties();
            Map<String, JsonSchemaItem> importProperties = importJsonSchema.getProperties();
            if (MapUtils.isNotEmpty(properties) || MapUtils.isNotEmpty(importProperties)) {
                List<String> dbJsonKeys = properties.keySet().stream().toList();
                List<String> importJsonKeys = importProperties.keySet().stream().toList();
                if (paramsIsSame(dbJsonKeys, importJsonKeys)) {
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
        if (StringUtils.equals(jsonSchema.getType(), PropertyConstant.ARRAY)) {
            JsonSchemaItem items = jsonSchema.getItems();
            JsonSchemaItem importItems = importJsonSchema.getItems();
            if (items != null && importItems != null) {
                if (!jsonSchemaIsSame(items, importItems)) {
                    return false;
                }
            }
        }
        return same;
    }

    private static boolean paramsIsSame(List<String> dbRestKeys, List<String> importRestKeys) {
        if (dbRestKeys.size() != importRestKeys.size()) {
            return true;
        }
        //看看是否有差集
        List<String> differenceRest = dbRestKeys.stream().filter(t -> !importRestKeys.contains(t)).toList();
        return !CollectionUtils.isEmpty(differenceRest);
    }

    private Boolean getFullCoverage(Boolean fullCoverage) {
        if (fullCoverage == null) {
            fullCoverage = false;
        }
        return fullCoverage;
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
        request.setProtocol(protocol);
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
                if (StringUtils.equalsIgnoreCase(treeNode.getParentId(), ModuleConstants.ROOT_NODE_PARENT_ID)) {
                    BaseTreeNode node = new BaseTreeNode(treeNode.getId(), treeNode.getName(), treeNode.getType(), treeNode.getParentId());
                    node.setPath(StringUtils.join("/", node.getName()));
                    baseTreeNodeList.add(node);
                    baseTreeNodeMap.put(treeNode.getId(), node);
                } else {
                    if (baseTreeNodeMap.containsKey(treeNode.getParentId())) {
                        BaseTreeNode node = new BaseTreeNode(treeNode.getId(), treeNode.getName(), treeNode.getType(), treeNode.getParentId());
                        node.setPath(StringUtils.join(baseTreeNodeMap.get(treeNode.getParentId()).getPath(), "/", node.getName()));
                        baseTreeNodeList.add(node);
                    }
                }
            }
            traverseList = notMatchedList;
        }
        return baseTreeNodeList;
    }

}
