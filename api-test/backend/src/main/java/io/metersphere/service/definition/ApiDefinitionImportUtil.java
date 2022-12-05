package io.metersphere.service.definition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.definition.*;
import io.metersphere.api.dto.definition.request.ElementUtil;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.definition.request.sampler.MsTCPSampler;
import io.metersphere.api.dto.mock.config.MockConfigImportDTO;
import io.metersphere.api.parse.api.ApiDefinitionImport;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.BaseProjectVersionMapper;
import io.metersphere.base.mapper.ext.ExtApiDefinitionMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.enums.ApiTestDataStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.i18n.Translator;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.request.ApiSyncCaseRequest;
import io.metersphere.service.BaseProjectApplicationService;
import io.metersphere.service.MockConfigService;
import io.metersphere.service.ext.ExtApiScheduleService;
import io.metersphere.xpack.api.service.ApiDefinitionSyncService;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.json.JSONObject;
import org.mybatis.spring.SqlSessionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ApiDefinitionImportUtil {

    private static final String SCHEDULE = "schedule";
    public static final String HEADERS = "headers";
    public static final String ARGUMENTS = "arguments";
    public static final String REST = "rest";
    public static final String BODY = "body";
    public static final String JSONSCHEMA = "jsonSchema";
    public static final String PROPERTIES = "properties";




    public static void checkUrl(ApiTestImportRequest request, Project project) {
        if (StringUtils.isNotBlank(request.getSwaggerUrl())) {
            if (!request.getPlatform().equalsIgnoreCase("Swagger2")) {
                sendFailMessage(request, project);
                MSException.throwException(Translator.get("file_format_does_not_meet_requirements"));
            }
            try {
                UrlTestUtils.testUrl(request.getSwaggerUrl(), 30000);
            } catch (Exception e) {
                sendFailMessage(request, project);
                MSException.throwException(e.getMessage());
            }
        }
    }

    public static void sendImportNotice(ApiTestImportRequest request, List<ApiImportSendNoticeDTO> apiImportSendNoticeDTOS, Project project) {
        ExtApiScheduleService scheduleService = CommonBeanFactory.getBean(ExtApiScheduleService.class);
        NoticeSendService noticeSendService = CommonBeanFactory.getBean(NoticeSendService.class);
        if (StringUtils.equals(request.getType(), SCHEDULE)) {
            String scheduleId = scheduleService.getScheduleInfo(request.getResourceId());
            String context = request.getSwaggerUrl() + "导入成功";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("url", request.getSwaggerUrl());
            NoticeModel noticeModel = NoticeModel.builder().operator(project.getCreateUser()).context(context).testId(scheduleId).subject(Translator.get("swagger_url_scheduled_import_notification")).paramMap(paramMap).event(NoticeConstants.Event.EXECUTE_SUCCESSFUL).build();
            noticeSendService.send(NoticeConstants.Mode.SCHEDULE, StringUtils.EMPTY, noticeModel);
        }
        if (!StringUtils.equals(request.getType(), SCHEDULE) && CollectionUtils.isNotEmpty(apiImportSendNoticeDTOS)) {
            for (ApiImportSendNoticeDTO apiImportSendNoticeDTO : apiImportSendNoticeDTOS) {
                ApiDefinitionResult apiDefinitionResult = apiImportSendNoticeDTO.getApiDefinitionResult();
                if (apiDefinitionResult != null && !apiDefinitionResult.isUpdated()) {
                    String context = SessionUtils.getUserId().concat("新建了接口定义").concat(":").concat(apiDefinitionResult.getName());
                    sendImportApiNotice(apiDefinitionResult, context, NoticeConstants.Event.CREATE, "api_create_notice");
                }
                if (apiDefinitionResult != null && apiDefinitionResult.isUpdated()) {
                    String context = SessionUtils.getUserId().concat("更新了接口定义").concat(":").concat(apiDefinitionResult.getName());
                    sendImportApiNotice(apiDefinitionResult, context, NoticeConstants.Event.UPDATE, "api_update_notice");
                }
                if (CollectionUtils.isNotEmpty(apiImportSendNoticeDTO.getCaseDTOList())) {
                    for (ApiTestCaseDTO apiTestCaseDTO : apiImportSendNoticeDTO.getCaseDTOList()) {
                        if (apiTestCaseDTO.isUpdated()) {
                            String context = SessionUtils.getUserId().concat("更新了接口用例").concat(":").concat(apiTestCaseDTO.getName());
                            sendImportCaseNotice(apiTestCaseDTO, context, NoticeConstants.Event.CASE_UPDATE, "api_case_update_notice");
                        } else {
                            String context = SessionUtils.getUserId().concat("新建了接口用例").concat(":").concat(apiTestCaseDTO.getName());
                            sendImportCaseNotice(apiTestCaseDTO, context, NoticeConstants.Event.CASE_CREATE, "api_case_create_notice");
                        }
                    }
                }
            }
        }
    }

    public static void checkFileSuffixName(ApiTestImportRequest request, String suffixName) {
        if (suffixName.equalsIgnoreCase("jmx")) {
            if (!request.getPlatform().equalsIgnoreCase("JMeter")) {
                MSException.throwException(Translator.get("file_format_does_not_meet_requirements"));
            }
        }
        if (suffixName.equalsIgnoreCase("har")) {
            if (!request.getPlatform().equalsIgnoreCase("Har")) {
                MSException.throwException(Translator.get("file_format_does_not_meet_requirements"));
            }
        }
        if (suffixName.equalsIgnoreCase("json")) {
            if (request.getPlatform().equalsIgnoreCase("Har") || request.getPlatform().equalsIgnoreCase("Jmeter")) {
                MSException.throwException(Translator.get("file_format_does_not_meet_requirements"));
            }
        }
    }

    public static void sendFailMessage(ApiTestImportRequest request, Project project) {
        ExtApiScheduleService scheduleService = CommonBeanFactory.getBean(ExtApiScheduleService.class);
        NoticeSendService noticeSendService = CommonBeanFactory.getBean(NoticeSendService.class);
        if (StringUtils.equals(request.getType(), SCHEDULE)) {
            String scheduleId = scheduleService.getScheduleInfo(request.getResourceId());
            String context = request.getSwaggerUrl() + "导入失败";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("url", request.getSwaggerUrl());
            paramMap.put("projectId", request.getProjectId());
            NoticeModel noticeModel = NoticeModel.builder().operator(project.getCreateUser()).context(context).testId(scheduleId).subject(Translator.get("swagger_url_scheduled_import_notification")).paramMap(paramMap).event(NoticeConstants.Event.EXECUTE_FAILED).build();
            noticeSendService.send(NoticeConstants.Mode.SCHEDULE, StringUtils.EMPTY, noticeModel);
        }
    }

    public static List<ApiImportSendNoticeDTO> importApi(ApiTestImportRequest request, ApiDefinitionImport apiImport, ApiDefinitionImportParamDTO apiDefinitionImportParamDTO) {
        BaseProjectApplicationService projectApplicationService = CommonBeanFactory.getBean(BaseProjectApplicationService.class);
        SqlSessionFactory sqlSessionFactory = apiDefinitionImportParamDTO.getSqlSessionFactory();
        ThreadLocal<Long> currentApiOrder = apiDefinitionImportParamDTO.getCurrentApiOrder();
        ThreadLocal<Long> currentApiCaseOrder = apiDefinitionImportParamDTO.getCurrentApiCaseOrder();
        BaseProjectVersionMapper baseProjectVersionMapper = apiDefinitionImportParamDTO.getBaseProjectVersionMapper();
        ProjectMapper projectMapper = apiDefinitionImportParamDTO.getProjectMapper();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        currentApiCaseOrder.remove();
        currentApiOrder.remove();
        String defaultVersion = baseProjectVersionMapper.getDefaultVersion(request.getProjectId());
        request.setDefaultVersion(defaultVersion);
        if (request.getVersionId() == null) {
            request.setVersionId(defaultVersion);
        }
        List<ApiDefinitionWithBLOBs> initData = apiImport.getData();

        Project project = projectMapper.selectByPrimaryKey(request.getProjectId());
        ProjectConfig config = projectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.URL_REPEATABLE.name());
        boolean urlRepeat = config.getUrlRepeatable();
        //过滤(一次只导入一个协议)
        List<ApiDefinitionWithBLOBs> filterData = initData.stream().filter(t -> t.getProtocol().equals(request.getProtocol())).collect(Collectors.toList());
        if (filterData.isEmpty()) {
            return new ArrayList<>();
        }
        //处理模块路径
        ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
        UpdateApiModuleDTO updateApiModuleDTO = apiModuleService.checkApiModule(request, apiImport, filterData, StringUtils.equals("fullCoverage", request.getModeId()), urlRepeat);
        //重新设置模块路径后的数据
        List<ApiDefinitionWithBLOBs> optionData = updateApiModuleDTO.getDefinitionWithBLOBs();
        // 新增的模块，key 为模块路径
        Map<String, ApiModule> moduleMap = updateApiModuleDTO.getModuleMap();
        //导入的处理重复后的case
        List<ApiTestCaseWithBLOBs> optionDataCases = updateApiModuleDTO.getCaseWithBLOBs();
        Boolean fullCoverage = updateApiModuleDTO.getFullCoverage();
        //所有模块的ID 及其全路径的map
        Map<String, String> idPathMap = updateApiModuleDTO.getIdPathMap();
        ApiModuleDTO chooseModule = updateApiModuleDTO.getChooseModule();

        Map<String, List<ApiTestCaseWithBLOBs>> apiIdCaseMap = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));

        ApiDefinitionMapper batchMapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        ApiTestCaseMapper apiTestCaseMapper = sqlSession.getMapper(ApiTestCaseMapper.class);
        ExtApiDefinitionMapper extApiDefinitionMapper = sqlSession.getMapper(ExtApiDefinitionMapper.class);
        ApiModuleMapper apiModuleMapper = sqlSession.getMapper(ApiModuleMapper.class);

        //系统内需要做更新操作的数据
        List<ApiDefinitionWithBLOBs> toUpdateList = new ArrayList<>();
        //与当前导入接口重复的系统内所有数据
        List<ApiDefinitionWithBLOBs> repeatList = new ArrayList<>();

        //处理导入数据与已存在数据关系
        if (request.getProtocol().equals("HTTP")) {
            if (urlRepeat) {
                repeatList = dealHttpUrlRepeat(chooseModule, idPathMap, optionData, fullCoverage, request, moduleMap, toUpdateList, optionDataCases, apiDefinitionImportParamDTO);
            } else {
                repeatList = dealHttpUrlNoRepeat(optionData, fullCoverage, request, moduleMap, toUpdateList, optionDataCases, apiDefinitionImportParamDTO);
            }
            if (optionData.isEmpty()) {
                moduleMap = new HashMap<>();
            }

        } else {
            Map<String, EsbApiParamsWithBLOBs> esbApiParamsMap = apiImport.getEsbApiParamsMap();

            repeatList = dealRepeat(request, optionData, moduleMap, fullCoverage, idPathMap, chooseModule, esbApiParamsMap, toUpdateList, optionDataCases, apiDefinitionImportParamDTO);
        }

        if (MapUtils.isNotEmpty(moduleMap)) {
            moduleMap.forEach((k,v)-> apiModuleMapper.insert(v));
        }
        int num = 0;
        if (!CollectionUtils.isEmpty(optionData) && optionData.get(0) != null && optionData.get(0).getProjectId() != null) {
            num = getNextNum(optionData.get(0).getProjectId(),extApiDefinitionMapper);
        }
        //如果需要导入的数据为空。此时清空mock信息
        if (optionData.isEmpty()) {
            apiImport.getMocks().clear();
        }
        List<ApiImportSendNoticeDTO> apiImportSendNoticeDTOS = new ArrayList<>();
        for (int i = 0; i < optionData.size(); i++) {
            ApiDefinitionWithBLOBs item = optionData.get(i);
            List<ApiDefinitionWithBLOBs> sameRefIds = null;
            //这里把数据库里与该接口相同的所有接口筛选出来包括不同的版本
            //如果 sameRefIds 与 toUpdates 相同，就用  toUpdates 代替 sameRefIds，因为toUpdates 可能会修改模块路径
            if (CollectionUtils.isNotEmpty(repeatList)) {
                sameRefIds = repeatList.stream().filter(t -> t.getRefId().equals(item.getRefId())).collect(Collectors.toList());
                List<String> repeatIds = sameRefIds.stream().map(ApiDefinition::getId).collect(Collectors.toList());
                List<ApiDefinitionWithBLOBs> toUpdates = toUpdateList.stream().filter(t -> t.getRefId().equals(item.getRefId())).collect(Collectors.toList());
                List<String> toUpDateIds = toUpdates.stream().map(ApiDefinition::getId).collect(Collectors.toList());
                List<String> reduce1 = repeatIds.stream().filter(t -> !toUpDateIds.contains(t)).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(reduce1)) {
                    sameRefIds = toUpdates;
                }
            }
            if (item.getVersionId().equalsIgnoreCase("trash")) {
                if (CollectionUtils.isNotEmpty(sameRefIds)) {
                    for (ApiDefinitionWithBLOBs sameRefId : sameRefIds) {
                        batchMapper.updateByPrimaryKey(sameRefId);
                    }
                }
                continue;
            }
            List<ApiTestCaseWithBLOBs> caseList = apiIdCaseMap.get(item.getId());
            setModule(item, apiDefinitionImportParamDTO.getApiModuleMapper());
            if (item.getName().length() > 255) {
                item.setName(item.getName().substring(0, 255));
            }
            //如果是创建版本数据，则num和其他版本数据一致
            if (item.getVersionId() == null || (!item.getVersionId().equals("new") && !item.getVersionId().equals("update"))) {
                item.setNum(num++);
            }
            //如果EsbData需要存储,则需要进行接口是否更新的判断
            ApiDefinitionImportParamDTO apiDefinitionImportParam = new ApiDefinitionImportParamDTO(item, request, apiImport.getMocks(), toUpdateList, caseList);
            apiDefinitionImportParam.setRepeatList(sameRefIds);
            if (apiImport.getEsbApiParamsMap() != null) {
                String apiId = item.getId();
                EsbApiParamsWithBLOBs model = apiImport.getEsbApiParamsMap().get(apiId);
                request.setModeId("fullCoverage");//标准版ESB数据导入不区分是否覆盖，默认都为覆盖

                ApiImportSendNoticeDTO apiImportSendNoticeDTO = importCreate(batchMapper, extApiDefinitionMapper, apiTestCaseMapper, apiDefinitionImportParam);
                if (model != null) {
                    apiImport.getEsbApiParamsMap().remove(apiId);
                    model.setResourceId(item.getId());
                    apiImport.getEsbApiParamsMap().put(item.getId(), model);
                }
                if (apiImportSendNoticeDTO != null) {
                    apiImportSendNoticeDTOS.add(apiImportSendNoticeDTO);
                }
            } else {
                ApiImportSendNoticeDTO apiImportSendNoticeDTO = importCreate(batchMapper, extApiDefinitionMapper, apiTestCaseMapper, apiDefinitionImportParam);
                if (apiImportSendNoticeDTO != null) {
                    apiImportSendNoticeDTOS.add(apiImportSendNoticeDTO);
                }
            }
            if (i % 300 == 0) {
                sqlSession.flushStatements();
            }
        }

        //判断EsbData是否需要存储
        if (apiImport.getEsbApiParamsMap() != null && apiImport.getEsbApiParamsMap().size() > 0) {
            EsbApiParamsMapper esbApiParamsMapper = sqlSession.getMapper(EsbApiParamsMapper.class);
            for (EsbApiParamsWithBLOBs model : apiImport.getEsbApiParamsMap().values()) {
                EsbApiParamsExample example = new EsbApiParamsExample();
                example.createCriteria().andResourceIdEqualTo(model.getResourceId());
                List<EsbApiParamsWithBLOBs> exitModelList = esbApiParamsMapper.selectByExampleWithBLOBs(example);
                if (exitModelList.isEmpty()) {
                    esbApiParamsMapper.insert(model);
                } else {
                    model.setId(exitModelList.get(0).getId());
                    esbApiParamsMapper.updateByPrimaryKeyWithBLOBs(model);
                }
            }
        }

        if (!CollectionUtils.isEmpty(apiImport.getMocks())) {
            MockConfigService mockConfigService = CommonBeanFactory.getBean(MockConfigService.class);
            mockConfigService.importMock(apiImport, sqlSession, request);
        }
        sqlSession.flushStatements();
        if (sqlSession != null && sqlSessionFactory != null) {
            SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        }

        return apiImportSendNoticeDTOS;
    }

    public static void sendImportApiNotice(ApiDefinitionWithBLOBs apiDefinitionWithBLOBs, String context, String event, String tip) {
        NoticeSendService noticeSendService = CommonBeanFactory.getBean(NoticeSendService.class);
        BeanMap beanMap = new BeanMap(apiDefinitionWithBLOBs);
        Map paramMap = new HashMap<>(beanMap);
        paramMap.put("operator", SessionUtils.getUserId());
        NoticeModel noticeModel = NoticeModel.builder().operator(SessionUtils.getUserId()).context(context).testId(apiDefinitionWithBLOBs.getId()).subject(Translator.get(tip)).paramMap(paramMap).excludeSelf(true).event(event).build();
        noticeSendService.send(NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
    }

    public static void sendImportCaseNotice(ApiTestCase apiTestCase, String context, String event, String tip) {
        NoticeSendService noticeSendService = CommonBeanFactory.getBean(NoticeSendService.class);
        BeanMap beanMap = new BeanMap(apiTestCase);
        Map paramMap = new HashMap<>(beanMap);
        paramMap.put("operator", SessionUtils.getUserId());
        NoticeModel noticeModel = NoticeModel.builder().operator(SessionUtils.getUserId()).context(context).testId(apiTestCase.getId()).subject(Translator.get(tip)).paramMap(paramMap).excludeSelf(true).event(event).build();
        noticeSendService.send(NoticeConstants.TaskType.API_DEFINITION_TASK, noticeModel);
    }

    private static List<ApiDefinitionWithBLOBs> dealHttpUrlRepeat(ApiModuleDTO chooseModule, Map<String, String> idPathMap, List<ApiDefinitionWithBLOBs> optionData,
                                                                  Boolean fullCoverage, ApiTestImportRequest request, Map<String, ApiModule> moduleMap,
                                                                  List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiTestCaseWithBLOBs> optionDataCases, ApiDefinitionImportParamDTO apiDefinitionImportParamDTO) {
        String updateVersionId = getUpdateVersionId(request);
        String versionId = getVersionId(request);
        Boolean fullCoverageApi = getFullCoverageApi(request);
        String projectId = request.getProjectId();
        //系统内重复的数据
        ExtApiDefinitionMapper extApiDefinitionMapper = apiDefinitionImportParamDTO.getExtApiDefinitionMapper();
        List<ApiDefinitionWithBLOBs> repeatApiDefinitionWithBLOBs = extApiDefinitionMapper.selectRepeatByBLOBs(optionData, projectId);

        //这个是名称加请求方式加路径加模块为key的map 就是为了去重
        Map<String, ApiDefinitionWithBLOBs> optionMap = new HashMap<>();
        //这个是系统内重复的数据
        Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap;
        //按照原来的顺序
        if (chooseModule != null) {
            //如果有选中的模块，则在选中的模块下过滤 过滤规则是 选择的模块路径+名称+method+path
            String chooseModuleParentId = getChooseModuleParentId(chooseModule);
            String chooseModulePath = getChooseModulePath(idPathMap, chooseModule, chooseModuleParentId);
            //这样的过滤规则下可能存在重复接口，如果是覆盖模块，需要按照去重规则再次去重，否则就加上接口原有的模块
            if (fullCoverage) {
                List<ApiDefinitionWithBLOBs> singleOptionData = new ArrayList<>();
                removeHttpChooseModuleRepeat(optionData, singleOptionData, chooseModulePath);
                optionData = singleOptionData;
                optionMap = optionData.stream().collect(Collectors.toMap(t -> t.getName().concat(t.getMethod()).concat(t.getPath()).concat(chooseModulePath), api -> api));
            } else {
                getChooseModuleUrlRepeatOptionMap(optionData, optionMap, chooseModulePath);
            }
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().filter(t -> t.getModuleId().equals(chooseModule.getId())).collect(Collectors.groupingBy(t -> t.getName().concat(t.getMethod()).concat(t.getPath()).concat(t.getModulePath())));
        } else {
            //否则在整个系统中过滤
            getUrlRepeatOptionMap(optionData, optionMap);
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.groupingBy(t -> t.getName().concat(t.getMethod()).concat(t.getPath()).concat(t.getModulePath())));
        }
        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap = new HashMap<>();
        //重复接口的case
        if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
            oldCaseMap = getOldCaseMap(repeatApiDefinitionWithBLOBs,apiDefinitionImportParamDTO.getApiTestCaseMapper());
        }
        //覆盖接口
        if (fullCoverage) {
            //允许覆盖模块，用导入的重复数据的最后一条覆盖查询的所有重复数据; case 在覆盖的时候，是拼接到原来的case，name唯一；不覆盖，就用原来的
            if (fullCoverageApi) {
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    startCoverModule(toUpdateList, optionData, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap, null);
                }
            } else {
                //覆盖但不覆盖模块
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    //过滤同一层级重复模块，导入文件没有新增接口无需创建接口模块
                    moduleMap = judgeModuleMap(moduleMap, optionMap, repeatDataMap);
                    startCover(toUpdateList, optionData, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap,null);
                }
            }
        } else {
            //不覆盖,同一接口不做更新;可能创建新版本，case也直接创建，
            if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                removeSameData(repeatDataMap, optionMap, optionData, moduleMap, versionId, optionDataCases);
            }
        }
        //最后在整个体统内检查一遍（防止在有选择的模块时，未找到重复，直接创建的情况）
        if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.groupingBy(t -> t.getName().concat(t.getMethod()).concat(t.getPath()).concat(t.getModulePath())));
            optionMap = optionData.stream().collect(Collectors.toMap(t -> t.getName().concat(t.getMethod()).concat(t.getPath()).concat(t.getModulePath()), api -> api));
            if (fullCoverage) {
                startCover(toUpdateList, optionData, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap,null);
            } else {
                //不覆盖,同一接口不做更新
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    removeSameData(repeatDataMap, optionMap, optionData, moduleMap, versionId, optionDataCases);
                }
            }
        }
        //将原来的case和更改的case组合在一起，为了同步的设置
        List<String> caseIds = optionDataCases.stream().map(ApiTestCase::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        buildCases(optionDataCases, oldCaseMap, caseIds);
        return repeatApiDefinitionWithBLOBs;
    }

    private static List<ApiDefinitionWithBLOBs> dealHttpUrlNoRepeat(List<ApiDefinitionWithBLOBs> optionData,
                                                                    Boolean fullCoverage, ApiTestImportRequest request, Map<String, ApiModule> moduleMap,
                                                                    List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiTestCaseWithBLOBs> optionDataCases, ApiDefinitionImportParamDTO apiDefinitionImportParamDTO) {

        //这个是名称加请求方式加路径加模块为key的map 就是为了去重
        Map<String, ApiDefinitionWithBLOBs> optionMap;

        String updateVersionId = getUpdateVersionId(request);
        String versionId = getVersionId(request);
        Boolean fullCoverageApi = getFullCoverageApi(request);
        String projectId = request.getProjectId();
        //系统内重复的数据
        ExtApiDefinitionMapper extApiDefinitionMapper = apiDefinitionImportParamDTO.getExtApiDefinitionMapper();
        List<ApiDefinitionWithBLOBs> repeatApiDefinitionWithBLOBs = extApiDefinitionMapper.selectRepeatByBLOBs(optionData, projectId);

        //这个是系统内重复的数据
        Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.groupingBy(t -> t.getMethod().concat(t.getPath())));

        //按照原来的顺序
        optionMap = optionData.stream().collect(Collectors.toMap(t -> t.getMethod().concat(t.getPath()), api -> api));

        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap = new HashMap<>();

        //重复接口的case
        if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
            oldCaseMap = getOldCaseMap(repeatApiDefinitionWithBLOBs,apiDefinitionImportParamDTO.getApiTestCaseMapper());
        }

        if (fullCoverage) {
            if (fullCoverageApi) {
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    startCoverModule(toUpdateList, optionData, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap, null);
                }
            } else {
                //不覆盖模块
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    startCover(toUpdateList, optionData, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap,null);
                }
            }
        } else {
            //不覆盖,同一接口不做更新
            if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                removeSameData(repeatDataMap, optionMap, optionData, moduleMap, versionId, optionDataCases);
            }
        }
        //将原来的case和更改的case组合在一起，为了同步的设置
        List<String> caseIds = optionDataCases.stream().map(ApiTestCase::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        buildCases(optionDataCases, oldCaseMap, caseIds);
        return repeatApiDefinitionWithBLOBs;
    }

    private static String getUpdateVersionId(ApiTestImportRequest request) {
        String updateVersionId;
        if (request.getUpdateVersionId() != null) {
            updateVersionId = request.getUpdateVersionId();
        } else {
            updateVersionId = request.getDefaultVersion();
        }
        return updateVersionId;
    }

    private static String getVersionId(ApiTestImportRequest request) {
        String versionId;
        if (request.getVersionId() == null) {
            versionId = request.getDefaultVersion();
        } else {
            versionId = request.getVersionId();
        }
        return versionId;
    }

    private static Boolean getFullCoverageApi(ApiTestImportRequest request) {
        Boolean fullCoverageApi = request.getCoverModule();
        if (fullCoverageApi == null) {
            fullCoverageApi = false;
        }
        return fullCoverageApi;
    }

    private static String getChooseModuleParentId(ApiModuleDTO chooseModule) {
        if (chooseModule.getParentId() == null) {
            chooseModule.setParentId(PropertyConstant.ROOT);
        }
        return chooseModule.getParentId();
    }

    private static String getChooseModulePath(Map<String, String> idPathMap, ApiModuleDTO chooseModule, String chooseModuleParentId) {
        String s;
        if (chooseModuleParentId.equals(PropertyConstant.ROOT)) {
            s = "/" + chooseModule.getName();
        } else {
            s = idPathMap.get(chooseModule.getId());
        }
        return s;
    }

    private static void removeHttpChooseModuleRepeat(List<ApiDefinitionWithBLOBs> optionData, List<ApiDefinitionWithBLOBs> singleOptionData, String chooseModulePath) {
        LinkedHashMap<String, List<ApiDefinitionWithBLOBs>> methodPathMap = optionData.stream().collect(Collectors.groupingBy(t -> t.getName().concat(t.getMethod()).concat(t.getPath()).concat(chooseModulePath), LinkedHashMap::new, Collectors.toList()));
        methodPathMap.forEach((k, v) -> singleOptionData.add(v.get(v.size() - 1)));
    }

    private static void getChooseModuleUrlRepeatOptionMap(List<ApiDefinitionWithBLOBs> optionData, Map<String, ApiDefinitionWithBLOBs> optionMap, String chooseModulePath) {
        for (ApiDefinitionWithBLOBs optionDatum : optionData) {
            if (optionDatum.getModulePath() == null) {
                optionMap.put(optionDatum.getName().concat(optionDatum.getMethod()).concat(optionDatum.getPath()).concat(chooseModulePath), optionDatum);
            } else {
                optionMap.put(optionDatum.getName().concat(optionDatum.getMethod()).concat(optionDatum.getPath()).concat(chooseModulePath).concat(optionDatum.getModulePath()), optionDatum);
            }
        }
    }

    private static void getUrlRepeatOptionMap(List<ApiDefinitionWithBLOBs> optionData, Map<String, ApiDefinitionWithBLOBs> optionMap) {
        for (ApiDefinitionWithBLOBs optionDatum : optionData) {
            if (optionDatum.getModulePath() == null) {
                optionMap.put(optionDatum.getName().concat(optionDatum.getMethod()).concat(optionDatum.getPath()), optionDatum);
            } else {
                optionMap.put(optionDatum.getName().concat(optionDatum.getMethod()).concat(optionDatum.getPath()).concat(optionDatum.getModulePath()), optionDatum);
            }
        }
    }

    private static Map<String, List<ApiTestCaseWithBLOBs>> getOldCaseMap(List<ApiDefinitionWithBLOBs> repeatApiDefinitionWithBLOBs, ApiTestCaseMapper apiTestCaseMapper) {
        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap;
        List<String> definitionIds = repeatApiDefinitionWithBLOBs.stream().map(ApiDefinition::getId).collect(Collectors.toList());
        ApiTestCaseExample testCaseExample = new ApiTestCaseExample();
        testCaseExample.createCriteria().andApiDefinitionIdIn(definitionIds);
        testCaseExample.or(testCaseExample.createCriteria().andStatusNotEqualTo(ApiTestDataStatus.TRASH.getValue()).andStatusIsNull());
        List<ApiTestCaseWithBLOBs> caseWithBLOBs = apiTestCaseMapper.selectByExampleWithBLOBs(testCaseExample);
        oldCaseMap = caseWithBLOBs.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
        return oldCaseMap;
    }

    private static void startCoverModule(List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiDefinitionWithBLOBs> optionData,
                                         Map<String, ApiDefinitionWithBLOBs> methodPathMap, Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap,
                                         String updateVersionId, List<ApiTestCaseWithBLOBs> optionDataCases,
                                         Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap, Map<String, EsbApiParamsWithBLOBs> esbApiParamsMap) {
        //临时记录修改数据后导入的数据
        List<ApiDefinitionWithBLOBs> coverApiList = new ArrayList<>();
        //临时记录需要更新的系统数据
        List<ApiDefinitionWithBLOBs> updateApiList = new ArrayList<>();
        repeatDataMap.forEach((k, v) -> {
            //导入的与系统是相同接口的数据
            ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = methodPathMap.get(k);
            if (apiDefinitionWithBLOBs != null) {
                //该接口的case
                Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
                Map<String, ApiTestCaseWithBLOBs> caseNameMap = getDistinctCaseNameMap(definitionIdCaseMAp, apiDefinitionWithBLOBs);
                //循环系统内重复接口
                int i = 0;
                ApiDefinitionWithBLOBs latestApi = null;
                for (ApiDefinitionWithBLOBs definitionWithBLOBs : v) {
                    if (definitionWithBLOBs.getLatest()) {
                        latestApi = definitionWithBLOBs;
                    }
                    if (!definitionWithBLOBs.getVersionId().equals(updateVersionId)) {
                        i += 1;
                        continue;
                    }
                    //组合case
                    if (MapUtils.isNotEmpty(caseNameMap)) {
                        buildCaseList(oldCaseMap, caseNameMap, definitionWithBLOBs, optionDataCases);
                    }
                    //在指定版本中更新接口内容并变更接口模块为当前导入选择的模块下创建导入文件中接口指定的模块
                    updateEsb(esbApiParamsMap, definitionWithBLOBs.getId(), apiDefinitionWithBLOBs.getId());
                    ApiDefinitionWithBLOBs api = new ApiDefinitionWithBLOBs();
                    BeanUtils.copyBean(api, apiDefinitionWithBLOBs);
                    api.setId(definitionWithBLOBs.getId());
                    setApiParam(api, updateVersionId, definitionWithBLOBs);
                    coverApiList.add(api);
                }
                if (i == v.size()) {
                    //如果系统内的所有版本都不是当前选择的数据更新版本，需要与lasted = 1 比较请求参数，参数一致，仅变更接口模块为当前导入接口的模块，不一致，新增并变更接口模块为当前导入接口的模块
                    if (latestApi != null) {
                        Boolean hasChange = checkIsSynchronize(latestApi, apiDefinitionWithBLOBs);
                        if (!hasChange) {
                            for (ApiDefinitionWithBLOBs definitionWithBLOBs : v) {
                                definitionWithBLOBs.setModuleId(apiDefinitionWithBLOBs.getModuleId());
                                definitionWithBLOBs.setModulePath(apiDefinitionWithBLOBs.getModulePath());
                                definitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
                                updateApiList.add(definitionWithBLOBs);
                            }
                            apiDefinitionWithBLOBs.setVersionId("trash");
                            //optionData.remove(apiDefinitionWithBLOBs);
                        } else {
                            addNewVersionApi(apiDefinitionWithBLOBs, latestApi, "update");
                            for (ApiDefinitionWithBLOBs definitionWithBLOBs : v) {
                                definitionWithBLOBs.setModuleId(apiDefinitionWithBLOBs.getModuleId());
                                definitionWithBLOBs.setModulePath(apiDefinitionWithBLOBs.getModulePath());
                                definitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
                                updateApiList.add(definitionWithBLOBs);
                            }
                        }
                    }
                } else {
                    for (ApiDefinitionWithBLOBs definitionWithBLOBs : v) {
                        definitionWithBLOBs.setModuleId(apiDefinitionWithBLOBs.getModuleId());
                        definitionWithBLOBs.setModulePath(apiDefinitionWithBLOBs.getModulePath());
                        definitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
                        updateApiList.add(definitionWithBLOBs);
                    }
                    optionData.remove(apiDefinitionWithBLOBs);
                }
            }
        });
        buildOtherParam(toUpdateList, optionData, coverApiList, updateApiList);
    }

    private static Map<String, ApiModule> judgeModuleMap(Map<String, ApiModule> moduleMap, Map<String, ApiDefinitionWithBLOBs> methodPathMap, Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap) {
        Set<String> repeatKeys = repeatDataMap.keySet();
        Set<String> importKeys = methodPathMap.keySet();
        List<String> repeatKeyList = new ArrayList<>(repeatKeys);
        List<String> importKeysList = new ArrayList<>(importKeys);
        List<String> intersection = repeatKeyList.stream().filter(importKeysList::contains).collect(Collectors.toList());
        if (intersection.size() == importKeysList.size()) {
            //导入文件没有新增接口无需创建接口模块
            moduleMap = new HashMap<>();
        }
        return moduleMap;
    }
    private static void addNewVersionApi(ApiDefinitionWithBLOBs apiDefinitionWithBLOBs, ApiDefinitionWithBLOBs v, String version) {
        apiDefinitionWithBLOBs.setVersionId(version);
        apiDefinitionWithBLOBs.setNum(v.getNum());
        apiDefinitionWithBLOBs.setStatus(v.getStatus());
        apiDefinitionWithBLOBs.setOrder(v.getOrder());
        apiDefinitionWithBLOBs.setRefId(v.getRefId());
        apiDefinitionWithBLOBs.setLatest(v.getLatest());
    }


    public static int getNextNum(String projectId, ExtApiDefinitionMapper extApiDefinitionMapper) {
        ApiDefinition apiDefinition = extApiDefinitionMapper.getNextNum(projectId);
        if (apiDefinition == null || apiDefinition.getNum() == null) {
            return 100001;
        } else {
            return Optional.of(apiDefinition.getNum() + 1).orElse(100001);
        }
    }
    private static ApiImportSendNoticeDTO importCreate(ApiDefinitionMapper batchMapper, ExtApiDefinitionMapper extApiDefinitionMapper, ApiTestCaseMapper apiTestCaseMapper, ApiDefinitionImportParamDTO apiDefinitionImportParamDTO) {
        apiDefinitionImportParamDTO.setExtApiDefinitionMapper(extApiDefinitionMapper);
        ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
        ApiImportSendNoticeDTO apiImportSendNoticeDTO = new ApiImportSendNoticeDTO();
        SaveApiDefinitionRequest saveReq = new SaveApiDefinitionRequest();
        ApiDefinitionWithBLOBs apiDefinition = apiDefinitionImportParamDTO.getApiDefinition();
        BeanUtils.copyBean(saveReq, apiDefinition);

        if (StringUtils.isEmpty(apiDefinition.getStatus())) {
            apiDefinition.setStatus(ApiTestDataStatus.UNDERWAY.getValue());
        }
        if (apiDefinition.getUserId() == null) {
            apiDefinition.setUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
        }

        apiDefinition.setDescription(apiDefinition.getDescription());
        List<ApiDefinitionWithBLOBs> collect = apiDefinitionImportParamDTO.getUpdateList().stream().filter(t -> t.getId().equals(apiDefinition.getId())).collect(Collectors.toList());
        apiDefinitionImportParamDTO.setUpdateList(collect);
        ApiTestImportRequest apiTestImportRequest = apiDefinitionImportParamDTO.getApiTestImportRequest();
        List<MockConfigImportDTO> mocks = apiDefinitionImportParamDTO.getMocks();
        List<ApiTestCaseWithBLOBs> caseList = apiDefinitionImportParamDTO.getCaseList();
        if (StringUtils.equals("fullCoverage", apiTestImportRequest.getModeId())) {
            return _importCreate(batchMapper, apiDefinitionService, apiTestCaseMapper, apiDefinitionImportParamDTO);
        } else if (StringUtils.equals("incrementalMerge", apiTestImportRequest.getModeId())) {
            if (CollectionUtils.isEmpty(collect)) {
                String originId = apiDefinition.getId();
                apiDefinition.setId(UUID.randomUUID().toString());
                apiDefinition.setCreateTime(System.currentTimeMillis());
                apiDefinition.setUpdateTime(System.currentTimeMillis());
                //postman 可能含有前置脚本，接口定义去掉脚本
                if (apiDefinition.getVersionId() != null && apiDefinition.getVersionId().equals("new")) {
                    apiDefinition.setLatest(apiTestImportRequest.getVersionId().equals(apiTestImportRequest.getDefaultVersion()));
                } else {
                    apiDefinition.setOrder(apiDefinitionService.getImportNextOrder(apiTestImportRequest.getProjectId()));
                    apiDefinition.setRefId(apiDefinition.getId());
                    apiDefinition.setLatest(true); // 新增的接口 latest = true
                }
                if (StringUtils.isNotEmpty(apiTestImportRequest.getVersionId())) {
                    apiDefinition.setVersionId(apiTestImportRequest.getVersionId());
                } else {
                    apiDefinition.setVersionId(apiTestImportRequest.getDefaultVersion());
                }
                boolean newCreate = !StringUtils.equals(ApiImportPlatform.Swagger2.name(), apiDefinitionImportParamDTO.getApiTestImportRequest().getPlatform())
                        && !StringUtils.isNotBlank(apiDefinitionImportParamDTO.getApiTestImportRequest().getSwaggerUrl())
                        && !StringUtils.equals("idea", apiDefinitionImportParamDTO.getApiTestImportRequest().getOrigin());
                caseList = setRequestAndAddNewCase(apiDefinition, caseList, newCreate);
                reSetImportMocksApiId(mocks, originId, apiDefinition.getId(), apiDefinition.getNum());
                batchMapper.insert(apiDefinition);
                List<ApiTestCaseDTO> apiTestCaseDTOS = importCase(apiDefinition, apiTestCaseMapper, caseList);
                apiImportSendNoticeDTO.setCaseDTOList(apiTestCaseDTOS);
                updateExitData(batchMapper, apiDefinitionImportParamDTO, apiDefinition);
            /*    extApiDefinitionMapper.clearLatestVersion(apiDefinition.getRefId());
                extApiDefinitionMapper.addLatestVersion(apiDefinition.getRefId());*/
                ApiDefinitionResult apiDefinitionResult = getApiDefinitionResult(apiDefinition, false);
                apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
                return apiImportSendNoticeDTO;
            } else {
                //不覆盖的接口，如果没有sameRequest则不导入。此时清空mock信息
                mocks.clear();
                return null;
            }
        } else {
            return _importCreate(batchMapper, apiDefinitionService, apiTestCaseMapper, apiDefinitionImportParamDTO);
        }
    }

    private static void updateExitData(ApiDefinitionMapper batchMapper, ApiDefinitionImportParamDTO apiDefinitionImportParamDTO, ApiDefinitionWithBLOBs apiDefinition) {
        if (apiDefinition.getLatest()) {
            List<ApiDefinitionWithBLOBs> repeatList = apiDefinitionImportParamDTO.getRepeatList();
            for (ApiDefinitionWithBLOBs apiDefinitionWithBLOBs : repeatList) {
                if (apiDefinitionWithBLOBs.getLatest()) {
                    apiDefinitionWithBLOBs.setLatest(false);
                }
                batchMapper.updateByPrimaryKey(apiDefinitionWithBLOBs);
            }
        } else {
            List<ApiDefinitionWithBLOBs> repeatList = apiDefinitionImportParamDTO.getRepeatList();
            for (ApiDefinitionWithBLOBs apiDefinitionWithBLOBs : repeatList) {
                batchMapper.updateByPrimaryKey(apiDefinitionWithBLOBs);
            }
        }
    }


    private static ApiDefinitionResult getApiDefinitionResult(ApiDefinitionWithBLOBs apiDefinition, boolean isUpdate) {
        ApiDefinitionResult apiDefinitionResult = new ApiDefinitionResult();
        BeanUtils.copyBean(apiDefinitionResult, apiDefinition);
        apiDefinitionResult.setUpdated(isUpdate);
        return apiDefinitionResult;
    }

    private static List<ApiTestCaseDTO> importCase(ApiDefinitionWithBLOBs apiDefinition, ApiTestCaseMapper apiTestCaseMapper, List<ApiTestCaseWithBLOBs> caseList) {
        ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
        ApiTestCaseService apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
        if (CollectionUtils.isEmpty(caseList)) {
            return new ArrayList<>();
        }
        List<ApiTestCaseDTO> apiTestCaseDTOS = new ArrayList<>();
        for (int i = 0; i < caseList.size(); i++) {
            ApiTestCaseDTO apiTestCaseDTO = new ApiTestCaseDTO();
            ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = caseList.get(i);
            apiTestCaseWithBLOBs.setApiDefinitionId(apiDefinition.getId());
            apiTestCaseWithBLOBs.setToBeUpdated(apiDefinition.getToBeUpdated() != null && apiDefinition.getToBeUpdated() && StringUtils.equalsIgnoreCase(apiTestCaseWithBLOBs.getVersionId(), "old_case"));
            apiTestCaseWithBLOBs.setVersionId(apiDefinition.getVersionId());
            if (apiTestCaseWithBLOBs.getCreateTime() == null) {
                apiTestCaseWithBLOBs.setCreateTime(System.currentTimeMillis());
            }
            apiTestCaseWithBLOBs.setUpdateTime(System.currentTimeMillis());

            if (StringUtils.isBlank(apiTestCaseWithBLOBs.getCaseStatus())) {
                apiTestCaseWithBLOBs.setCaseStatus(ApiTestDataStatus.PREPARE.getValue());
            }
            if (StringUtils.isBlank(apiTestCaseWithBLOBs.getCreateUserId())) {
                apiTestCaseWithBLOBs.setCreateUserId(apiDefinition.getUserId());
            }
            if (apiTestCaseWithBLOBs.getOrder() == null) {
                apiTestCaseWithBLOBs.setOrder(apiDefinitionService.getImportNextCaseOrder(apiDefinition.getProjectId()));
            }
            if (apiTestCaseWithBLOBs.getNum() == null) {
                apiTestCaseWithBLOBs.setNum(apiTestCaseService.getNextNum(apiDefinition.getId(), apiDefinition.getNum() + i, apiDefinition.getProjectId()));
            }

            if (apiDefinition.getToBeUpdateTime() != null) {
                apiTestCaseWithBLOBs.setToBeUpdateTime(apiDefinition.getToBeUpdateTime());
            }

            if (StringUtils.isBlank(apiTestCaseWithBLOBs.getPriority())) {
                apiTestCaseWithBLOBs.setPriority("P0");
            }
            apiTestCaseWithBLOBs.setStatus(StringUtils.EMPTY);

            if (StringUtils.isNotBlank(apiTestCaseWithBLOBs.getId())) {
                BeanUtils.copyBean(apiTestCaseDTO, apiTestCaseWithBLOBs);
                apiTestCaseDTO.setUpdated(true);
                apiTestCaseMapper.updateByPrimaryKeyWithBLOBs(apiTestCaseWithBLOBs);
            } else {
                apiTestCaseWithBLOBs.setId(UUID.randomUUID().toString());
                apiTestCaseWithBLOBs.setCreateUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
                apiTestCaseWithBLOBs.setUpdateUserId(Objects.requireNonNull(SessionUtils.getUser()).getId());
                BeanUtils.copyBean(apiTestCaseDTO, apiTestCaseWithBLOBs);
                apiTestCaseDTO.setUpdated(false);
                apiTestCaseMapper.insert(apiTestCaseWithBLOBs);
            }
            apiTestCaseDTOS.add(apiTestCaseDTO);
        }
        return apiTestCaseDTOS;
    }

    private static List<ApiDefinitionWithBLOBs> dealRepeat(ApiTestImportRequest request, List<ApiDefinitionWithBLOBs> optionData, Map<String, ApiModule> moduleMap, Boolean fullCoverage, Map<String, String> idPathMap, ApiModuleDTO chooseModule,
                                                           Map<String, EsbApiParamsWithBLOBs> esbApiParamsMap, List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiTestCaseWithBLOBs> optionDataCases, ApiDefinitionImportParamDTO apiDefinitionImportParamDTO) {
        String chooseModuleId = request.getModuleId();
        List<ApiDefinitionWithBLOBs> repeatApiDefinitionWithBLOBs = getApiDefinitionWithBLOBsList(request, optionData, apiDefinitionImportParamDTO.getExtApiDefinitionMapper());
        //重复接口的case
        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
            oldCaseMap = getOldCaseMap(repeatApiDefinitionWithBLOBs, apiDefinitionImportParamDTO.getApiTestCaseMapper());
        }
        Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap = null;
        Map<String, ApiDefinitionWithBLOBs> optionMap = new HashMap<>();

        if (chooseModule != null) {
            String chooseModuleParentId = getChooseModuleParentId(chooseModule);
            String chooseModulePath = getChooseModulePath(idPathMap, chooseModule, chooseModuleParentId);
            if (fullCoverage) {
                List<ApiDefinitionWithBLOBs> singleOptionData = new ArrayList<>();
                removeOtherChooseModuleRepeat(optionData, singleOptionData, chooseModulePath);
                optionData = singleOptionData;
                optionMap = optionData.stream().collect(Collectors.toMap(t -> t.getName().concat(chooseModulePath), api -> api));
            } else {
                getNoHChooseModuleUrlRepeatOptionMap(optionData, optionMap, chooseModulePath);
            }
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().filter(t -> t.getModuleId().equals(chooseModuleId)).collect(Collectors.groupingBy(t -> t.getName().concat(t.getModulePath())));
        } else {
            buildOptionMap(optionData, optionMap);
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.groupingBy(t -> t.getName().concat(t.getModulePath())));
        }
        Boolean fullCoverageApi = getFullCoverageApi(request);
        String updateVersionId = getUpdateVersionId(request);
        String versionId = getVersionId(request);
        //处理数据
        if (fullCoverage) {
            if (fullCoverageApi) {
                startCoverModule(toUpdateList, optionData, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap,esbApiParamsMap);
                //coverModule(toUpdateList, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap, optionData);
            } else {
                //过滤同一层级重复模块，导入文件没有新增接口无需创建接口模块
                moduleMap = judgeModuleMap(moduleMap, optionMap, repeatDataMap);
                startCover(toUpdateList, optionData, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap,esbApiParamsMap);
                //moduleMap = cover(moduleMap, toUpdateList, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap, esbApiParamsMap,optionData);
            }
        } else {
            //不覆盖
            if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                removeSameData(repeatDataMap, optionMap, optionData, moduleMap, versionId, optionDataCases);
            }
            //removeRepeat(optionData, optionMap, repeatDataMap, moduleMap, versionId, optionDataCases);
        }

        //系统内检查重复
        if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.groupingBy(t -> t.getName().concat(t.getModulePath())));
            optionMap = optionData.stream().collect(Collectors.toMap(t -> t.getName().concat(t.getModulePath()), api -> api));
            if (fullCoverage) {
                startCover(toUpdateList, optionData, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap,esbApiParamsMap);
               // cover(moduleMap, toUpdateList, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap, esbApiParamsMap);
            } else {
                //不覆盖,同一接口不做更新
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    removeSameData(repeatDataMap, optionMap, optionData, moduleMap, versionId, optionDataCases);
                }
            }
        }

        if (optionData.isEmpty()) {
            moduleMap = new HashMap<>();
        }
        //将原来的case和更改的case组合在一起，为了同步的设置
        List<String> caseIds = optionDataCases.stream().map(ApiTestCase::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        buildCases(optionDataCases, oldCaseMap, caseIds);

        return repeatApiDefinitionWithBLOBs;
    }

    private void removeRepeat(List<ApiDefinitionWithBLOBs> optionData, Map<String, ApiDefinitionWithBLOBs> nameModuleMap,
                              Map<String, ApiDefinitionWithBLOBs> repeatDataMap, Map<String, ApiModule> moduleMap,
                              String versionId,
                              List<ApiTestCaseWithBLOBs> optionDataCases) {
        if (MapUtils.isEmpty(nameModuleMap) || MapUtils.isEmpty(repeatDataMap)) {
            return;
        }
        Map<String, List<ApiDefinitionWithBLOBs>> moduleOptionData = optionData.stream().collect(Collectors.groupingBy(ApiDefinition::getModulePath));
        repeatDataMap.forEach((k, v) -> {
            ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = nameModuleMap.get(k);
            if (apiDefinitionWithBLOBs == null) {
                return;
            }
            Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
            List<ApiTestCaseWithBLOBs> distinctNameCases = definitionIdCaseMAp.get(apiDefinitionWithBLOBs.getId());
            String modulePath = apiDefinitionWithBLOBs.getModulePath();
            List<ApiDefinitionWithBLOBs> moduleData = moduleOptionData.get(modulePath);
            if (moduleData != null && moduleData.size() <= 1) {
                moduleMap.remove(modulePath);
                removeModulePath(moduleMap, moduleOptionData, modulePath);
                moduleData.remove(apiDefinitionWithBLOBs);
            }
            //不覆盖选择版本，如果被选版本有同接口，不导入，否则创建新版本接口
            if (v.getVersionId().equals(versionId)) {
                optionData.remove(apiDefinitionWithBLOBs);
                if (CollectionUtils.isNotEmpty(distinctNameCases)) {
                    distinctNameCases.forEach(optionDataCases::remove);
                }
            } else {
                //这里是为了标识当前数据是需要创建版本的，不是全新增的数据
                addNewVersionApi(apiDefinitionWithBLOBs, v, "new");
            }
        });
    }

    private Map<String, ApiModule> cover(Map<String, ApiModule> moduleMap, List<ApiDefinitionWithBLOBs> toUpdateList,
                                         Map<String, ApiDefinitionWithBLOBs> nameModuleMap, Map<String, ApiDefinitionWithBLOBs> repeatDataMap,
                                         String updateVersionId, List<ApiTestCaseWithBLOBs> optionDataCases,
                                         Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap, Map<String, EsbApiParamsWithBLOBs> esbApiParamsMap) {
        //覆盖但不覆盖模块
        if (MapUtils.isEmpty(nameModuleMap) || MapUtils.isEmpty(repeatDataMap)) {
            return moduleMap;
        }
        //导入文件没有新增接口无需创建接口模块
        moduleMap = judgeModule(moduleMap, nameModuleMap, repeatDataMap);
        repeatDataMap.forEach((k, v) -> {
            ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = nameModuleMap.get(k);
            if (apiDefinitionWithBLOBs != null) {
                //系统内重复的数据的版本如果不是选择的数据更新版本，则在数据更新版本新增，否则更新这个版本的数据
                if (!v.getVersionId().equals(updateVersionId)) {
                    addNewVersionApi(apiDefinitionWithBLOBs, v, "update");
                    return;
                }
                Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
                //该接口的case
                Map<String, ApiTestCaseWithBLOBs> caseNameMap = getDistinctCaseNameMap(definitionIdCaseMAp, apiDefinitionWithBLOBs);
                updateEsb(esbApiParamsMap, v.getId(), apiDefinitionWithBLOBs.getId());
                //组合case
                if (MapUtils.isNotEmpty(caseNameMap)) {
                    buildCaseList(oldCaseMap, caseNameMap, v, optionDataCases);
                }
                apiDefinitionWithBLOBs.setId(v.getId());
                apiDefinitionWithBLOBs.setModuleId(v.getModuleId());
                apiDefinitionWithBLOBs.setModulePath(v.getModulePath());
                setApiParam(apiDefinitionWithBLOBs, updateVersionId, v);
                toUpdateList.add(v);
            }
        });
        return moduleMap;
    }

    private void coverModule(List<ApiDefinitionWithBLOBs> toUpdateList, Map<String, ApiDefinitionWithBLOBs> nameModuleMap,
                             Map<String, ApiDefinitionWithBLOBs> repeatDataMap, String updateVersionId, List<ApiTestCaseWithBLOBs> optionDataCases,
                             Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap, Map<String, EsbApiParamsWithBLOBs> esbApiParamsMap,List<ApiDefinitionWithBLOBs> optionData) {
        if (MapUtils.isEmpty(nameModuleMap) || MapUtils.isEmpty(repeatDataMap)) {
            return;
        }
        repeatDataMap.forEach((k, v) -> {
            ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = nameModuleMap.get(k);
            if (apiDefinitionWithBLOBs != null) {
                //系统内重复的数据的版本如果不是选择的数据更新版本，则在数据更新版本新增，否则更新这个版本的数据
                if (!v.getVersionId().equals(updateVersionId)) {
                    Boolean aBoolean = checkIsSynchronize(v, apiDefinitionWithBLOBs);
                    if (!aBoolean) {
                        v.setModuleId(apiDefinitionWithBLOBs.getModuleId());
                        v.setModulePath(apiDefinitionWithBLOBs.getModulePath());
                        BeanUtils.copyBean(apiDefinitionWithBLOBs, v);
                    } else {
                        addNewVersionApi(apiDefinitionWithBLOBs, v, "update");
                    }
                    return;
                }
                //该接口的case
                Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
                Map<String, ApiTestCaseWithBLOBs> caseNameMap = getDistinctCaseNameMap(definitionIdCaseMAp, apiDefinitionWithBLOBs);
                updateEsb(esbApiParamsMap, v.getId(), apiDefinitionWithBLOBs.getId());
                //组合case
                if (MapUtils.isNotEmpty(caseNameMap)) {
                    buildCaseList(oldCaseMap, caseNameMap, v, optionDataCases);
                }
                apiDefinitionWithBLOBs.setId(v.getId());
                setApiParam(apiDefinitionWithBLOBs, updateVersionId, v);
                toUpdateList.add(v);
            }
        });
    }


    private static void removeOtherChooseModuleRepeat(List<ApiDefinitionWithBLOBs> optionData, List<ApiDefinitionWithBLOBs> singleOptionData, String chooseModulePath) {
        LinkedHashMap<String, List<ApiDefinitionWithBLOBs>> methodPathMap = optionData.stream().collect(Collectors.groupingBy(t -> t.getName().concat(chooseModulePath), LinkedHashMap::new, Collectors.toList()));
        methodPathMap.forEach((k, v) -> {
            singleOptionData.add(v.get(v.size() - 1));
        });
    }

    private static void buildOptionMap(List<ApiDefinitionWithBLOBs> optionData, Map<String, ApiDefinitionWithBLOBs> optionMap) {
        for (ApiDefinitionWithBLOBs optionDatum : optionData) {
            if (optionDatum.getModulePath() == null) {
                optionMap.put(optionDatum.getName(), optionDatum);
            } else {
                optionMap.put(optionDatum.getName().concat(optionDatum.getModulePath()), optionDatum);
            }
        }
    }

    private Map<String, ApiModule> judgeModule(Map<String, ApiModule> moduleMap, Map<String, ApiDefinitionWithBLOBs> nameModuleMap, Map<String, ApiDefinitionWithBLOBs> repeatDataMap) {
        AtomicBoolean remove = new AtomicBoolean(true);

        if (repeatDataMap.size() >= nameModuleMap.size()) {
            repeatDataMap.forEach((k, v) -> {
                ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = nameModuleMap.get(k);
                if (apiDefinitionWithBLOBs == null) {
                    remove.set(false);
                }
            });
            if (remove.get()) {
                moduleMap = new HashMap<>();
            }
        }
        return moduleMap;
    }

    private static void getNoHChooseModuleUrlRepeatOptionMap(List<ApiDefinitionWithBLOBs> optionData, Map<String, ApiDefinitionWithBLOBs> optionMap, String chooseModulePath) {
        for (ApiDefinitionWithBLOBs optionDatum : optionData) {
            if (optionDatum.getModulePath() == null) {
                optionMap.put(optionDatum.getName().concat(chooseModulePath), optionDatum);
            } else {
                optionMap.put(optionDatum.getName().concat(chooseModulePath).concat(optionDatum.getModulePath()), optionDatum);
            }
        }
    }

    private static List<ApiDefinitionWithBLOBs> getApiDefinitionWithBLOBsList(ApiTestImportRequest request, List<ApiDefinitionWithBLOBs> optionData, ExtApiDefinitionMapper extApiDefinitionMapper) {
        //处理数据
        List<String> nameList = optionData.stream().map(ApiDefinitionWithBLOBs::getName).collect(Collectors.toList());
        String projectId = request.getProjectId();
        String protocol = request.getProtocol();
        //获取系统内重复数据
        return extApiDefinitionMapper.selectRepeatByProtocol(nameList, protocol, projectId);
    }

    private static void startCover(List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiDefinitionWithBLOBs> optionData,
                                   Map<String, ApiDefinitionWithBLOBs> methodPathMap, Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap,
                                   String updateVersionId, List<ApiTestCaseWithBLOBs> optionDataCases,
                                   Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap, Map<String, EsbApiParamsWithBLOBs> esbApiParamsMap) {
        List<ApiDefinitionWithBLOBs> coverApiList = new ArrayList<>();
        List<ApiDefinitionWithBLOBs> updateApiList = new ArrayList<>();
        repeatDataMap.forEach((k, v) -> {
            ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = methodPathMap.get(k);
            if (apiDefinitionWithBLOBs != null) {
                //该接口的case
                Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
                Map<String, ApiTestCaseWithBLOBs> caseNameMap = getDistinctCaseNameMap(definitionIdCaseMAp, apiDefinitionWithBLOBs);
                int i = 0;
                ApiDefinitionWithBLOBs latestApi = null;
                for (ApiDefinitionWithBLOBs definitionWithBLOBs : v) {
                    if (definitionWithBLOBs.getLatest()) {
                        latestApi = definitionWithBLOBs;
                    }
                    if (!definitionWithBLOBs.getVersionId().equals(updateVersionId)) {
                        i += 1;
                        continue;
                    }
                    updateEsb(esbApiParamsMap, definitionWithBLOBs.getId(), apiDefinitionWithBLOBs.getId());
                    //组合case
                    if (MapUtils.isNotEmpty(caseNameMap)) {
                        buildCaseList(oldCaseMap, caseNameMap, definitionWithBLOBs, optionDataCases);
                    }

                    ApiDefinitionWithBLOBs api = new ApiDefinitionWithBLOBs();
                    BeanUtils.copyBean(api, apiDefinitionWithBLOBs);
                    api.setId(definitionWithBLOBs.getId());
                    api.setModuleId(definitionWithBLOBs.getModuleId());
                    api.setModulePath(definitionWithBLOBs.getModulePath());
                    setApiParam(api, updateVersionId, definitionWithBLOBs);
                    coverApiList.add(api);
                    updateApiList.add(definitionWithBLOBs);
                }
                if (i == v.size()) {
                    if (latestApi!=null) {
                        Boolean hasChange = checkIsSynchronize(latestApi, apiDefinitionWithBLOBs);
                        if (!hasChange) {
                            optionData.remove(apiDefinitionWithBLOBs);
                        } else {
                            //如果系统内的所有版本都不是当前选择的数据更新版本，则在数据更新版本这里新建数据
                            //未开启模块覆盖 指定版本没有数据 请求参数数据不一致  将接口内容创建到系统原模块中的接口指定版本
                            addNewVersionApi(apiDefinitionWithBLOBs, latestApi, "update");
                            apiDefinitionWithBLOBs.setModuleId(latestApi.getModuleId());
                            apiDefinitionWithBLOBs.setModulePath(latestApi.getModulePath());
                        }
                    }
                } else {
                    optionData.remove(apiDefinitionWithBLOBs);
                }
            }
        });
        buildOtherParam(toUpdateList, optionData, coverApiList, updateApiList);
    }

    private static Map<String, ApiTestCaseWithBLOBs> getDistinctCaseNameMap(Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp, ApiDefinitionWithBLOBs apiDefinitionWithBLOBs) {
        if (MapUtils.isEmpty(definitionIdCaseMAp)) {
            return null;
        }
        List<ApiTestCaseWithBLOBs> caseWithBLOBs = definitionIdCaseMAp.get(apiDefinitionWithBLOBs.getId());
        if (CollectionUtils.isNotEmpty(caseWithBLOBs)) {
            return caseWithBLOBs.stream().filter(t -> !StringUtils.equalsIgnoreCase("old_case", t.getVersionId())).collect(Collectors.toMap(ApiTestCase::getName, testCase -> testCase));
        } else {
            return null;
        }
    }

    private static void buildCaseList(Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap,
                                      Map<String, ApiTestCaseWithBLOBs> caseNameMap,
                                      ApiDefinitionWithBLOBs definitionWithBLOBs, List<ApiTestCaseWithBLOBs> optionDataCases) {
        //找出系统内重复接口的case，表里可能一个接口有多个同名case的可能
        List<ApiTestCaseWithBLOBs> oldApiTestCases = oldCaseMap.get(definitionWithBLOBs.getId());

        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseNameMap;
        //如果同名重复用例有多个，则覆盖最后的那个
        if (CollectionUtils.isNotEmpty(oldApiTestCases)) {
            oldCaseNameMap = oldApiTestCases.stream().collect(Collectors.groupingBy(ApiTestCase::getName));
            caseNameMap.forEach((name, importCaseWithBLOBs) -> {
                //如果导入的有重名，覆盖，接口ID替换成系统内的
                importCaseWithBLOBs.setApiDefinitionId(definitionWithBLOBs.getId());
                List<ApiTestCaseWithBLOBs> caseWithBLOBs = oldCaseNameMap.get(name);
                if (CollectionUtils.isNotEmpty(caseWithBLOBs)) {
                    for (int i = 0; i < caseWithBLOBs.size(); i++) {
                        int version = 0;
                        if (caseWithBLOBs.get(i).getVersion() != null) {
                            version = caseWithBLOBs.get(i).getVersion() + 1;
                        }
                        if (i == 0) {
                            //被覆盖数据
                            importCaseWithBLOBs.setId(caseWithBLOBs.get(i).getId());
                            importCaseWithBLOBs.setNum(caseWithBLOBs.get(i).getNum());
                            importCaseWithBLOBs.setVersion(version);
                            importCaseWithBLOBs.setCreateUserId(caseWithBLOBs.get(i).getCreateUserId());
                            importCaseWithBLOBs.setUpdateUserId(caseWithBLOBs.get(i).getCreateUserId());
                        } else {
                            //同名的旧数据处理
                            caseWithBLOBs.get(i).setVersionId("old_case");
                            optionDataCases.add(caseWithBLOBs.get(i));
                        }
                    }
                    oldCaseNameMap.remove(name);
                }
            });
            //不同名的旧数据处理
            oldCaseNameMap.forEach((k, v) -> {
                if (CollectionUtils.isNotEmpty(v)) {
                    for (ApiTestCaseWithBLOBs apiTestCaseWithBLOBs : v) {
                        apiTestCaseWithBLOBs.setVersionId("old_case");
                        optionDataCases.add(apiTestCaseWithBLOBs);
                    }
                }
            });
        } else {
            //否则直接给新增用例赋值新的接口ID
            caseNameMap.forEach((name, caseWithBLOBs1) -> {
                caseWithBLOBs1.setApiDefinitionId(definitionWithBLOBs.getId());
                caseWithBLOBs1.setVersion(0);
            });
        }
    }

    private static void setApiParam(ApiDefinitionWithBLOBs apiDefinitionWithBLOBs, String versionId, ApiDefinitionWithBLOBs definitionWithBLOBs) {
        apiDefinitionWithBLOBs.setVersionId(versionId);
        apiDefinitionWithBLOBs.setNum(definitionWithBLOBs.getNum());
        apiDefinitionWithBLOBs.setStatus(definitionWithBLOBs.getStatus());
        apiDefinitionWithBLOBs.setOrder(definitionWithBLOBs.getOrder());
        apiDefinitionWithBLOBs.setRefId(definitionWithBLOBs.getRefId());
        apiDefinitionWithBLOBs.setLatest(definitionWithBLOBs.getLatest());
        apiDefinitionWithBLOBs.setCreateTime(definitionWithBLOBs.getCreateTime());
        apiDefinitionWithBLOBs.setUpdateTime(definitionWithBLOBs.getUpdateTime());
    }

    private static void buildOtherParam(List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiDefinitionWithBLOBs> optionData, List<ApiDefinitionWithBLOBs> coverApiList, List<ApiDefinitionWithBLOBs> updateApiList) {
        optionData.addAll(coverApiList);
        toUpdateList.addAll(updateApiList);

    }

    private static void removeSameData(Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap, Map<String, ApiDefinitionWithBLOBs> methodPathMap,
                                       List<ApiDefinitionWithBLOBs> optionData, Map<String, ApiModule> moduleMap, String versionId,
                                       List<ApiTestCaseWithBLOBs> optionDataCases) {

        Map<String, List<ApiDefinitionWithBLOBs>> moduleOptionData = optionData.stream().collect(Collectors.groupingBy(ApiDefinition::getModulePath));
        repeatDataMap.forEach((k, v) -> {
            ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = methodPathMap.get(k);
            if (apiDefinitionWithBLOBs != null) {
                Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
                List<ApiTestCaseWithBLOBs> distinctNameCases = definitionIdCaseMAp.get(apiDefinitionWithBLOBs.getId());
                String modulePath = apiDefinitionWithBLOBs.getModulePath();
                List<ApiDefinitionWithBLOBs> moduleData = moduleOptionData.get(modulePath);
                if (moduleData != null && moduleData.size() <= 1) {
                    moduleMap.remove(modulePath);
                    removeModulePath(moduleMap, moduleOptionData, modulePath);
                    moduleData.remove(apiDefinitionWithBLOBs);
                }
                //不覆盖同一接口不做更新 注意原接口的update时间不变
                optionData.remove(apiDefinitionWithBLOBs);
                if (CollectionUtils.isNotEmpty(distinctNameCases)) {
                    distinctNameCases.forEach(optionDataCases::remove);
                }
            }
        });
    }

    private static void removeModulePath(Map<String, ApiModule> moduleMap, Map<String, List<ApiDefinitionWithBLOBs>> moduleOptionData, String modulePath) {
        ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
        if (StringUtils.isBlank(modulePath)) {
            return;
        }
        String[] pathTree = apiModuleService.getPathTree(modulePath);
        String lastPath = pathTree[pathTree.length - 1];
        String substring = modulePath.substring(0, modulePath.indexOf("/" + lastPath));
        if (moduleOptionData.get(substring) == null || moduleOptionData.get(substring).size() == 0) {
            moduleMap.remove(substring);
            removeModulePath(moduleMap, moduleOptionData, substring);
        }

    }

    private static void buildCases(List<ApiTestCaseWithBLOBs> optionDataCases, Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap, List<String> caseIds) {
        if (MapUtils.isNotEmpty(oldCaseMap)) {
            List<ApiTestCaseWithBLOBs> oldCaseList = new ArrayList<>();
            Collection<List<ApiTestCaseWithBLOBs>> values = oldCaseMap.values();
            for (List<ApiTestCaseWithBLOBs> value : values) {
                oldCaseList.addAll(value);
            }
            List<ApiTestCaseWithBLOBs> collect = oldCaseList.stream().filter(t -> !caseIds.contains(t.getId())).collect(Collectors.toList());
            optionDataCases.addAll(collect);
        }
    }



    private static void updateEsb(Map<String, EsbApiParamsWithBLOBs> esbApiParamsMap, String newId, String oldId) {
        if (MapUtils.isNotEmpty(esbApiParamsMap)) {
            EsbApiParamsWithBLOBs esbApiParamsWithBLOBs = esbApiParamsMap.get(oldId);
            if (esbApiParamsWithBLOBs != null) {
                esbApiParamsMap.remove(oldId);
                esbApiParamsWithBLOBs.setResourceId(newId);
                esbApiParamsMap.put(newId, esbApiParamsWithBLOBs);
            }
        }
    }

    private static ApiImportSendNoticeDTO _importCreate(ApiDefinitionMapper batchMapper, ApiDefinitionService apiDefinitionService, ApiTestCaseMapper apiTestCaseMapper, ApiDefinitionImportParamDTO apiDefinitionImportParamDTO) {
        ApiDefinitionWithBLOBs apiDefinition = apiDefinitionImportParamDTO.getApiDefinition();
        ApiTestImportRequest apiTestImportRequest = apiDefinitionImportParamDTO.getApiTestImportRequest();
        List<ApiDefinitionWithBLOBs> sameRequest = apiDefinitionImportParamDTO.getUpdateList();
        List<MockConfigImportDTO> mocks = apiDefinitionImportParamDTO.getMocks();
        List<ApiTestCaseWithBLOBs> caseList = apiDefinitionImportParamDTO.getCaseList();
        String originId = apiDefinition.getId();
        ApiImportSendNoticeDTO apiImportSendNoticeDTO = new ApiImportSendNoticeDTO();
        if (CollectionUtils.isEmpty(sameRequest)) {
            // 没有这个接口 新增
            apiDefinition.setId(UUID.randomUUID().toString());

            apiDefinition.setCreateTime(System.currentTimeMillis());
            apiDefinition.setUpdateTime(System.currentTimeMillis());
            if (apiDefinition.getVersionId() != null && apiDefinition.getVersionId().equals("update")) {
                if (StringUtils.isNotEmpty(apiTestImportRequest.getUpdateVersionId())) {
                    apiDefinition.setVersionId(apiTestImportRequest.getUpdateVersionId());
                } else {
                    apiDefinition.setVersionId(apiTestImportRequest.getDefaultVersion());
                }
                apiDefinition.setLatest(apiTestImportRequest.getVersionId().equals(apiTestImportRequest.getDefaultVersion()));
            } else {
                apiDefinition.setRefId(apiDefinition.getId());
                apiDefinition.setLatest(true); // 新增接口 latest = true
                apiDefinition.setOrder(apiDefinitionService.getImportNextOrder(apiTestImportRequest.getProjectId()));
                if (StringUtils.isNotEmpty(apiTestImportRequest.getVersionId())) {
                    apiDefinition.setVersionId(apiTestImportRequest.getVersionId());
                } else {
                    apiDefinition.setVersionId(apiTestImportRequest.getDefaultVersion());
                }
            }

            reSetImportMocksApiId(mocks, originId, apiDefinition.getId(), apiDefinition.getNum());
            boolean newCreate = !StringUtils.equals(ApiImportPlatform.Swagger2.name(), apiDefinitionImportParamDTO.getApiTestImportRequest().getPlatform())
                    && !StringUtils.isNotBlank(apiDefinitionImportParamDTO.getApiTestImportRequest().getSwaggerUrl())
                    && !StringUtils.equals("idea", apiDefinitionImportParamDTO.getApiTestImportRequest().getOrigin());
            caseList = setRequestAndAddNewCase(apiDefinition, caseList, newCreate);
            batchMapper.insert(apiDefinition);
            ApiDefinitionResult apiDefinitionResult = getApiDefinitionResult(apiDefinition, false);
            apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
            List<ApiTestCaseDTO> apiTestCaseDTOS = importCase(apiDefinition, apiTestCaseMapper, caseList);
            apiImportSendNoticeDTO.setCaseDTOList(apiTestCaseDTOS);
        } else { //如果存在则修改
            if (StringUtils.isEmpty(apiTestImportRequest.getUpdateVersionId())) {
                apiTestImportRequest.setUpdateVersionId(apiTestImportRequest.getDefaultVersion());
            }
            Optional<ApiDefinitionWithBLOBs> apiOp = sameRequest.stream().filter(api -> StringUtils.equals(api.getVersionId(), apiTestImportRequest.getUpdateVersionId())).findFirst();

            if (apiOp.isEmpty()) {
                apiDefinition.setId(UUID.randomUUID().toString());
                apiDefinition.setCreateTime(System.currentTimeMillis());
                apiDefinition.setUpdateTime(System.currentTimeMillis());
                if (!apiDefinition.getVersionId().equals("update")) {
                    if (sameRequest.get(0).getRefId() != null) {
                        apiDefinition.setRefId(sameRequest.get(0).getRefId());
                    } else {
                        apiDefinition.setRefId(apiDefinition.getId());
                    }
                    apiDefinition.setNum(sameRequest.get(0).getNum()); // 使用第一个num当作本次的num
                    apiDefinition.setOrder(sameRequest.get(0).getOrder());
                }
                //apiDefinition.setLatest(apiTestImportRequest.getVersionId().equals(apiTestImportRequest.getDefaultVersion()));
                apiDefinition.setVersionId(apiTestImportRequest.getUpdateVersionId());

                if (sameRequest.get(0).getUserId() != null) {
                    apiDefinition.setUserId(sameRequest.get(0).getUserId());
                }
                batchMapper.insert(apiDefinition);
                ApiDefinitionResult apiDefinitionResult = getApiDefinitionResult(apiDefinition, false);
                apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
                List<ApiTestCaseDTO> apiTestCaseDTOS = importCase(apiDefinition, apiTestCaseMapper, caseList);
                apiImportSendNoticeDTO.setCaseDTOList(apiTestCaseDTOS);
            } else {
                ApiDefinitionWithBLOBs existApi = apiOp.get();
                apiDefinition.setStatus(existApi.getStatus());
                apiDefinition.setOriginalState(existApi.getOriginalState());
                apiDefinition.setCaseStatus(existApi.getCaseStatus());
                apiDefinition.setNum(existApi.getNum()); //id 不变
                if (existApi.getRefId() != null) {
                    apiDefinition.setRefId(existApi.getRefId());
                } else {
                    apiDefinition.setRefId(apiDefinition.getId());
                }
                apiDefinition.setVersionId(apiTestImportRequest.getUpdateVersionId());
                if (existApi.getUserId() != null) {
                    apiDefinition.setUserId(existApi.getUserId());
                }
                apiDefinition.setUpdateTime(System.currentTimeMillis());
                if (!StringUtils.equalsIgnoreCase(apiTestImportRequest.getPlatform(), ApiImportPlatform.Metersphere.name())) {
                    apiDefinition.setTags(existApi.getTags()); // 其他格式 tag 不变，MS 格式替换
                }
                apiDefinition.setId(existApi.getId());
                setRequestAndAddNewCase(apiDefinition, caseList, false);
                apiDefinition.setOrder(existApi.getOrder());
                reSetImportMocksApiId(mocks, originId, apiDefinition.getId(), apiDefinition.getNum());
                batchMapper.updateByPrimaryKeyWithBLOBs(apiDefinition);
                ApiDefinitionResult apiDefinitionResult = getApiDefinitionResult(apiDefinition, true);
                apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
                List<ApiTestCaseDTO> apiTestCaseDTOS = importCase(apiDefinition, apiTestCaseMapper, caseList);
                apiImportSendNoticeDTO.setCaseDTOList(apiTestCaseDTOS);
            }
        }
        updateExitData(batchMapper, apiDefinitionImportParamDTO, apiDefinition);
        /*extApiDefinitionMapper.clearLatestVersion(apiDefinition.getRefId());
        extApiDefinitionMapper.addLatestVersion(apiDefinition.getRefId());*/
        return apiImportSendNoticeDTO;
    }

    private static void setModule(ApiDefinitionWithBLOBs item, ApiModuleMapper apiModuleMapper) {
        if (item != null && StringUtils.isEmpty(item.getModuleId()) || "default-module".equals(item.getModuleId())) {
            ApiModuleExample example = new ApiModuleExample();
            example.createCriteria().andProjectIdEqualTo(item.getProjectId()).andProtocolEqualTo(item.getProtocol()).andNameEqualTo("未规划接口");
            List<ApiModule> modules = apiModuleMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(modules)) {
                item.setModuleId(modules.get(0).getId());
                item.setModulePath(modules.get(0).getName());
            }
        }
    }



    private static List<ApiTestCaseWithBLOBs> setRequestAndAddNewCase(ApiDefinitionWithBLOBs apiDefinition, List<ApiTestCaseWithBLOBs> caseList, boolean newCreate) {
        boolean createCase = false;
        if (StringUtils.equalsIgnoreCase(apiDefinition.getProtocol(), RequestTypeConstants.HTTP)) {
            createCase = setImportHashTree(apiDefinition);
        } else if (StringUtils.equalsIgnoreCase(apiDefinition.getProtocol(), RequestTypeConstants.TCP)) {
            createCase = setImportTCPHashTree(apiDefinition);
        }
        if (newCreate && createCase && CollectionUtils.isEmpty(caseList)) {
            ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = addNewCase(apiDefinition);
            caseList = new ArrayList<>();
            caseList.add(apiTestCaseWithBLOBs);
        }
        return caseList;
    }





    public static Boolean checkIsSynchronize(ApiDefinitionWithBLOBs existApi, ApiDefinitionWithBLOBs apiDefinition) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        ApiSyncCaseRequest apiSyncCaseRequest = new ApiSyncCaseRequest();
        Boolean toUpdate = false;
        ApiDefinitionSyncService apiDefinitionSyncService = CommonBeanFactory.getBean(ApiDefinitionSyncService.class);
        if (apiDefinitionSyncService != null) {
            toUpdate = apiDefinitionSyncService.getProjectApplications(existApi.getProjectId());
            apiSyncCaseRequest = apiDefinitionSyncService.getApiSyncCaseRequest(existApi.getProjectId());
        }
        //Compare the basic information of the API. If it contains the comparison that needs to be done for the synchronization information,
        // put the data into the to-be-synchronized
        Boolean diffBasicInfo = delBasicInfo(existApi, apiDefinition, apiSyncCaseRequest, toUpdate);
        if (diffBasicInfo != null) return diffBasicInfo;

        Boolean diffApiRequest = delRequest(existApi, apiDefinition, objectMapper, apiSyncCaseRequest, toUpdate);
        if (diffApiRequest != null) return diffApiRequest;

        Boolean diffResponse = delResponse(existApi, apiDefinition, objectMapper);
        if (diffResponse != null) return diffResponse;
        return false;
    }

    private static Boolean delRequest(ApiDefinitionWithBLOBs existApi, ApiDefinitionWithBLOBs apiDefinition, ObjectMapper objectMapper, ApiSyncCaseRequest apiSyncCaseRequest, Boolean toUpdate) {
        JsonNode exApiRequest = null;
        JsonNode apiRequest = null;
        try {
            exApiRequest = objectMapper.readTree(existApi.getRequest());
            apiRequest = objectMapper.readTree(apiDefinition.getRequest());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (exApiRequest == null || apiRequest == null) {
            return exApiRequest != null || apiRequest != null;
        }

        List<String> compareProList = Arrays.asList(HEADERS, ARGUMENTS, REST);

        Map<String, Boolean> applicationMap = new HashMap<>(4);
        applicationMap.put(HEADERS, apiSyncCaseRequest.getHeaders());
        applicationMap.put(ARGUMENTS, apiSyncCaseRequest.getQuery());
        applicationMap.put(REST, apiSyncCaseRequest.getRest());
        applicationMap.put(BODY, apiSyncCaseRequest.getBody());

        boolean diffByNodes = false;
        for (String property : compareProList) {
            JsonNode exApiJsonNode = exApiRequest.get(property);
            JsonNode apiJsonNode = apiRequest.get(property);
            if (exApiJsonNode != null && apiJsonNode != null) {
                diffByNodes = getDiffByArrayNodes(apiRequest, exApiRequest, objectMapper, property);
                if (diffByNodes) {
                    if (toUpdate && applicationMap.get(property)) {
                        apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
                    }
                    break;
                }
            }
        }
        if (diffByNodes) {
            return true;
        }
        return delBody(apiDefinition, objectMapper, toUpdate, exApiRequest, apiRequest, applicationMap);
    }

    private static Boolean delBody(ApiDefinitionWithBLOBs apiDefinition, ObjectMapper objectMapper, Boolean toUpdate, JsonNode exApiRequest, JsonNode apiRequest, Map<String, Boolean> applicationMap) {
        JsonNode exBodyNode = exApiRequest.get(BODY);
        JsonNode bodyNode = apiRequest.get(BODY);
        if (exBodyNode != null && bodyNode != null) {
            JsonNode exRowNode = exBodyNode.get("raw");
            JsonNode rowNode = bodyNode.get("raw");
            if (exRowNode != null && rowNode != null) {
                if (!StringUtils.equals(exRowNode.asText(), rowNode.asText())) {
                    if (applicationMap.get(BODY)) {
                        apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
                    }
                    return true;
                }
            }

            boolean diffByNodes = getDiffByArrayNodes(bodyNode, exBodyNode, objectMapper, "kvs");
            if (diffByNodes && toUpdate && applicationMap.get(BODY)) {
                apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
            }
            if (diffByNodes) {
                return true;
            }
            JsonNode exApiJsonSchema = exBodyNode.get(JSONSCHEMA);
            JsonNode apiJsonSchema = bodyNode.get(JSONSCHEMA);
            if (exApiJsonSchema == null || apiJsonSchema == null) {
                return false;
            }

            JsonNode exApiProperties = exApiJsonSchema.get(PROPERTIES);
            JsonNode apiProperties = apiJsonSchema.get(PROPERTIES);
            if (exApiProperties == null || apiProperties == null) {
                return false;
            }
            boolean diffJsonschema = replenishCaseProperties(exApiProperties, apiProperties);
            if (diffJsonschema && toUpdate && applicationMap.get(BODY)) {
                apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
            }
            return diffJsonschema;
        }
        return null;
    }

    private static boolean replenishCaseProperties(JsonNode exApiProperties, JsonNode apiProperties) {

        Iterator<Map.Entry<String, JsonNode>> apiFields = apiProperties.fields();
        Iterator<Map.Entry<String, JsonNode>> exApiFields = exApiProperties.fields();
        boolean diffProp = false;
        while (apiFields.hasNext()) {
            Map.Entry<String, JsonNode> apiNode = apiFields.next();
            if (diffProp) {
                break;
            }
            if (exApiFields.hasNext()) {
                Map.Entry<String, JsonNode> exChildNode = null;
                Map.Entry<String, JsonNode> exNode = exApiFields.next();
                if (StringUtils.equalsIgnoreCase(apiNode.getKey(), exNode.getKey())) {
                    exChildNode = exNode;
                } else {
                    diffProp = true;
                }
                if (exChildNode == null) {
                    continue;
                }
                JsonNode value = apiNode.getValue();
                JsonNode value1 = exChildNode.getValue();
                JsonNode apiPropertiesNode = value.get(PROPERTIES);
                JsonNode exApiPropertiesNode = value1.get(PROPERTIES);
                if (apiPropertiesNode == null || exApiPropertiesNode == null) {
                    continue;
                }
                replenishCaseProperties(exApiPropertiesNode, apiPropertiesNode);
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * 比较导入的与系统中重复的两个api的基础信息
     *
     * @param existApi
     * @param apiDefinition
     * @param apiSyncCaseRequest
     * @param toUpdate
     * @return
     */
    private static Boolean delBasicInfo(ApiDefinitionWithBLOBs existApi, ApiDefinitionWithBLOBs apiDefinition, ApiSyncCaseRequest apiSyncCaseRequest, Boolean toUpdate) {
        if (!StringUtils.equals(apiDefinition.getMethod(), existApi.getMethod())) {
            if (apiSyncCaseRequest.getMethod() && toUpdate) {
                apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
            }
            return true;
        }
        if (!StringUtils.equals(apiDefinition.getProtocol(), existApi.getProtocol())) {
            if (apiSyncCaseRequest.getProtocol() && toUpdate) {
                apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
            }
            return true;
        }

        if (!StringUtils.equals(apiDefinition.getPath(), existApi.getPath())) {
            if (apiSyncCaseRequest.getPath() && toUpdate) {
                apiDefinition.setToBeUpdateTime(System.currentTimeMillis());
            }
            return true;
        }
        if (!StringUtils.equals(apiDefinition.getCreateUser(), existApi.getCreateUser())) {
            return true;
        }

        if (!StringUtils.equals(apiDefinition.getStatus(), existApi.getStatus()) && StringUtils.isNotBlank(existApi.getStatus()) && StringUtils.isNotBlank(apiDefinition.getStatus())) {
            return true;
        }

        if (!StringUtils.equals(apiDefinition.getTags(), existApi.getTags())) {
            if (apiDefinition.getTags() != null && Objects.equals(apiDefinition.getTags(), StringUtils.EMPTY) && existApi.getTags() != null && Objects.equals(existApi.getTags(), StringUtils.EMPTY)) {
                return true;
            }
        }

        if (!StringUtils.equals(existApi.getRemark(), apiDefinition.getRemark()) && StringUtils.isNotBlank(existApi.getRemark()) && StringUtils.isNotBlank(apiDefinition.getRemark())) {
            return true;
        }

        if (!StringUtils.equals(existApi.getDescription(), apiDefinition.getDescription()) && StringUtils.isNotBlank(existApi.getDescription()) && StringUtils.isNotBlank(apiDefinition.getDescription())) {
            return true;
        }
        return null;
    }


    /**
     * 比较导入的与系统中重复的两个api的响应体信息
     *
     * @param existApi
     * @param apiDefinition
     * @param objectMapper
     * @return
     */
    private static Boolean delResponse(ApiDefinitionWithBLOBs existApi, ApiDefinitionWithBLOBs apiDefinition, ObjectMapper objectMapper) {
        JsonNode exApiResponse = null;
        JsonNode apiResponse = null;

        if (StringUtils.isBlank(apiDefinition.getResponse()) || StringUtils.isBlank(existApi.getResponse())) {
            return !StringUtils.isBlank(apiDefinition.getResponse()) || !StringUtils.isBlank(existApi.getResponse());
        }

        try {
            exApiResponse = objectMapper.readTree(existApi.getResponse());
            apiResponse = objectMapper.readTree(apiDefinition.getResponse());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if (exApiResponse == null || apiResponse == null) {
            return exApiResponse != null || apiResponse != null;
        }

        if (exApiResponse.get(HEADERS) != null && apiResponse.get(HEADERS) != null) {
            if (!StringUtils.equals(exApiResponse.get(HEADERS).toString(), apiResponse.get(HEADERS).toString())) {
                return true;
            }
        }

        if (exApiResponse.get(PropertyConstant.TYPE) != null && apiResponse.get(PropertyConstant.TYPE) != null) {
            if (!StringUtils.equals(exApiResponse.get(PropertyConstant.TYPE).toString(), apiResponse.get(PropertyConstant.TYPE).toString())) {
                return true;
            }
        }

        if (exApiResponse.get("name") != null && apiResponse.get("name") != null) {
            if (!StringUtils.equals(exApiResponse.get("name").toString(), apiResponse.get("name").toString())) {
                return true;
            }
        }

        if (exApiResponse.get(BODY) != null && apiResponse.get(BODY) != null) {
            if (!StringUtils.equals(exApiResponse.get(BODY).toString(), apiResponse.get(BODY).toString())) {
                return true;
            }
        }

        if (exApiResponse.get("statusCode") != null && apiResponse.get("statusCode") != null) {
            if (!StringUtils.equals(exApiResponse.get("statusCode").toString(), apiResponse.get("statusCode").toString())) {
                return true;
            }
        }

        if (exApiResponse.get("enable") != null && apiResponse.get("enable") != null) {
            return !StringUtils.equals(exApiResponse.get("enable").toString(), apiResponse.get("enable").toString());
        }
        return null;
    }

    private static boolean getDiffByArrayNodes(JsonNode apiRequest, JsonNode exApiRequest, ObjectMapper objectMapper, String name) {
        JsonNode apiNameNode = apiRequest.get(name);
        JsonNode caseNameNode = exApiRequest.get(name);
        if (apiNameNode == null || caseNameNode == null) {
            return false;
        }
        Map<String, String> apiMap = new HashMap<>();
        getKeyNameMap(apiNameNode, objectMapper, apiMap, "name");
        Map<String, String> exApiMap = new HashMap<>();
        getKeyNameMap(caseNameNode, objectMapper, exApiMap, "name");
        if (apiMap.size() != exApiMap.size()) {
            return true;
        }
        Set<String> apiKey = apiMap.keySet();
        Set<String> exApiKey = exApiMap.keySet();
        List<String> collect = apiKey.stream().filter(exApiKey::contains).collect(Collectors.toList());
        if (collect.size() != apiKey.size()) {
            return true;
        }
        return false;
    }

    public static void getKeyNameMap(JsonNode apiNameNode, ObjectMapper objectMapper, Map<String, String> nameMap, String nodeKey) {
        for (int i = 0; i < apiNameNode.size(); i++) {
            JsonNode apiName = apiNameNode.get(i);
            if (apiName.has(nodeKey)) {
                String keyName = null;
                try {
                    keyName = objectMapper.writeValueAsString(apiName.get(nodeKey));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                if (StringUtils.isNotBlank(keyName) && !StringUtils.equals(keyName, "\"\"") && !StringUtils.equals(keyName, "null")) {
                    try {
                        nameMap.put(apiName.get(nodeKey).toString(), objectMapper.writeValueAsString(apiName));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static void reSetImportMocksApiId(List<MockConfigImportDTO> mocks, String originId, String newId, int apiNum) {
        if (CollectionUtils.isNotEmpty(mocks)) {
            int index = 1;
            for (MockConfigImportDTO item : mocks) {
                if (StringUtils.equals(item.getApiId(), originId)) {
                    item.setApiId(newId);
                }
                item.setExpectNum(apiNum + "_" + index);
                index++;
            }
        }
    }

    private static ApiTestCaseWithBLOBs addNewCase(ApiDefinitionWithBLOBs apiDefinition) {
        ApiTestCaseWithBLOBs apiTestCase = new ApiTestCaseWithBLOBs();
        apiTestCase.setApiDefinitionId(apiDefinition.getId());
        apiTestCase.setProjectId(apiDefinition.getProjectId());
        apiTestCase.setName(apiDefinition.getName());
        apiTestCase.setRequest(apiDefinition.getRequest());
        return apiTestCase;
    }

    private static boolean setImportHashTree(ApiDefinitionWithBLOBs apiDefinition) {
        String request = apiDefinition.getRequest();
        JSONObject jsonObject = JSONUtil.parseObject(request);
        ElementUtil.dataFormatting(jsonObject);
        MsHTTPSamplerProxy msHTTPSamplerProxy = JSONUtil.parseObject(jsonObject.toString(), MsHTTPSamplerProxy.class);
        boolean createCase = CollectionUtils.isNotEmpty(msHTTPSamplerProxy.getHeaders());
        if (CollectionUtils.isNotEmpty(msHTTPSamplerProxy.getArguments()) && !createCase) {
            createCase = true;
        }
        if (msHTTPSamplerProxy.getBody() != null && !createCase) {
            createCase = true;
        }
        if (CollectionUtils.isNotEmpty(msHTTPSamplerProxy.getRest()) && !createCase) {
            createCase = true;
        }
        msHTTPSamplerProxy.setId(apiDefinition.getId());
        msHTTPSamplerProxy.setHashTree(new LinkedList<>());
        apiDefinition.setRequest(JSON.toJSONString(msHTTPSamplerProxy));
        return createCase;
    }

    private static boolean setImportTCPHashTree(ApiDefinitionWithBLOBs apiDefinition) {
        String request = apiDefinition.getRequest();
        MsTCPSampler tcpSampler = JSON.parseObject(request, MsTCPSampler.class);
        boolean createCase = CollectionUtils.isNotEmpty(tcpSampler.getParameters());
        if (StringUtils.isNotBlank(tcpSampler.getJsonDataStruct()) && !createCase) {
            createCase = true;
        }
        if (StringUtils.isNotBlank(tcpSampler.getRawDataStruct()) && !createCase) {
            createCase = true;
        }
        if (CollectionUtils.isNotEmpty(tcpSampler.getXmlDataStruct()) && !createCase) {
            createCase = true;
        }
        tcpSampler.setId(apiDefinition.getId());
        tcpSampler.setHashTree(new LinkedList<>());
        apiDefinition.setRequest(JSON.toJSONString(tcpSampler));
        return createCase;
    }
}
