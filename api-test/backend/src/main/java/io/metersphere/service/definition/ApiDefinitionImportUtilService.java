package io.metersphere.service.definition;

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
import io.metersphere.base.mapper.ext.ExtApiTestCaseMapper;
import io.metersphere.commons.constants.*;
import io.metersphere.commons.enums.ApiTestDataStatus;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.user.SessionUser;
import io.metersphere.commons.utils.*;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.dto.UserDTO;
import io.metersphere.i18n.Translator;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.api.DefinitionReference;
import io.metersphere.notice.sender.NoticeModel;
import io.metersphere.notice.service.NoticeSendService;
import io.metersphere.service.BaseProjectApplicationService;
import io.metersphere.service.BaseUserService;
import io.metersphere.service.MockConfigService;
import io.metersphere.service.ServiceUtils;
import io.metersphere.service.ext.ExtApiScheduleService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiDefinitionImportUtilService {

    private static final String SCHEDULE = "schedule";

    public static final String BODY = "body";

    private final ThreadLocal<Long> currentApiOrder = new ThreadLocal<>();
    private final ThreadLocal<Long> currentApiCaseOrder = new ThreadLocal<>();

    @Resource
    private ExtApiScheduleService extApiScheduleService;
    @Resource
    private NoticeSendService noticeSendService;
    @Resource
    private BaseProjectApplicationService projectApplicationService;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private BaseProjectVersionMapper baseProjectVersionMapper;
    @Resource
    private ProjectMapper projectMapper;

    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ApiTestCaseMapper apiTestCaseMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ApiTestCaseService apiTestCaseService;
    @Resource
    private BaseUserService baseUserService;
    @Resource
    private ScheduleMapper scheduleMapper;


    public void checkUrl(ApiTestImportRequest request, Project project) {
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

    public void sendImportNotice(ApiTestImportRequest request, List<ApiImportSendNoticeDTO> apiImportSendNoticeDTOS, Project project) {
        if (StringUtils.equals(request.getType(), SCHEDULE)) {
            String scheduleId = extApiScheduleService.getScheduleInfo(request.getResourceId());
            UserDTO userDTO = baseUserService.getUserDTO(request.getUserId());
            String context = request.getSwaggerUrl() + "导入成功";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("url", request.getSwaggerUrl());
            paramMap.put("operator", userDTO.getName());
            NoticeModel noticeModel = NoticeModel.builder().operator(project.getCreateUser()).context(context).testId(scheduleId).subject(Translator.get("swagger_url_scheduled_import_notification")).paramMap(paramMap).event(NoticeConstants.Event.EXECUTE_SUCCESSFUL).build();
            noticeSendService.send(NoticeConstants.Mode.SCHEDULE, StringUtils.EMPTY, noticeModel);
        }
        if (!StringUtils.equals(request.getType(), SCHEDULE) && CollectionUtils.isNotEmpty(apiImportSendNoticeDTOS)) {
            for (ApiImportSendNoticeDTO apiImportSendNoticeDTO : apiImportSendNoticeDTOS) {
                ApiDefinitionResult apiDefinitionResult = apiImportSendNoticeDTO.getApiDefinitionResult();
                if (apiDefinitionResult != null && !apiDefinitionResult.isUpdated()) {
                    String context = SessionUtils.getUserId().concat("新建了接口定义").concat(":").concat(apiDefinitionResult.getName());
                    ApiDefinitionImportUtil.sendImportApiNotice(apiDefinitionResult, context, NoticeConstants.Event.CREATE, "api_create_notice");
                }
                if (apiDefinitionResult != null && apiDefinitionResult.isUpdated()) {
                    String context = SessionUtils.getUserId().concat("更新了接口定义").concat(":").concat(apiDefinitionResult.getName());
                    ApiDefinitionImportUtil.sendImportApiNotice(apiDefinitionResult, context, NoticeConstants.Event.UPDATE, "api_update_notice");
                }
                if (CollectionUtils.isNotEmpty(apiImportSendNoticeDTO.getCaseDTOList())) {
                    for (ApiTestCaseDTO apiTestCaseDTO : apiImportSendNoticeDTO.getCaseDTOList()) {
                        if (apiTestCaseDTO.isUpdated()) {
                            String context = SessionUtils.getUserId().concat("更新了接口用例").concat(":").concat(apiTestCaseDTO.getName());
                            ApiDefinitionImportUtil.sendImportCaseNotice(apiTestCaseDTO, context, NoticeConstants.Event.CASE_UPDATE, "api_case_update_notice");
                        } else {
                            String context = SessionUtils.getUserId().concat("新建了接口用例").concat(":").concat(apiTestCaseDTO.getName());
                            ApiDefinitionImportUtil.sendImportCaseNotice(apiTestCaseDTO, context, NoticeConstants.Event.CASE_CREATE, "api_case_create_notice");
                        }
                    }
                }
            }
        }
    }

    public void checkFileSuffixName(ApiTestImportRequest request, String suffixName) {
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

    public void sendFailMessage(ApiTestImportRequest request, Project project) {
        if (StringUtils.equals(request.getType(), SCHEDULE)) {
            String scheduleId = extApiScheduleService.getScheduleInfo(request.getResourceId());
            UserDTO userDTO = baseUserService.getUserDTO(request.getUserId());
            String context = request.getSwaggerUrl() + "导入失败";
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("url", request.getSwaggerUrl());
            paramMap.put("projectId", request.getProjectId());
            paramMap.put("operator", userDTO.getName());
            NoticeModel noticeModel = NoticeModel.builder().operator(project.getCreateUser()).context(context).testId(scheduleId).subject(Translator.get("swagger_url_scheduled_import_notification")).paramMap(paramMap).event(NoticeConstants.Event.EXECUTE_FAILED).build();
            noticeSendService.send(NoticeConstants.Mode.SCHEDULE, StringUtils.EMPTY, noticeModel);
        }
    }

    public List<ApiImportSendNoticeDTO> importApi(ApiTestImportRequest request, ApiDefinitionImport apiImport) {
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
        ApiModuleService apiModuleService = CommonBeanFactory.getBean(ApiModuleService.class);
        //处理模块路径
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

        ApiDefinitionMapper batchMapper = sqlSession.getMapper(ApiDefinitionMapper.class);
        ExtApiDefinitionMapper extApiDefinitionMapper = sqlSession.getMapper(ExtApiDefinitionMapper.class);
        ApiModuleMapper apiModuleMapper = sqlSession.getMapper(ApiModuleMapper.class);
        OperatingLogMapper operatingLogMapper = sqlSession.getMapper(OperatingLogMapper.class);
        OperatingLogResourceMapper operatingLogResourceMapper = sqlSession.getMapper(OperatingLogResourceMapper.class);
        //系统内需要做更新操作的数据
        List<ApiDefinitionWithBLOBs> toUpdateList = new ArrayList<>();
        //与当前导入接口重复的系统内所有数据
        List<ApiDefinitionWithBLOBs> repeatList;
        ApiImportParamDto apiImportParamDto = new ApiImportParamDto(chooseModule, idPathMap, optionData, fullCoverage, request, moduleMap, toUpdateList, optionDataCases);
        apiImportParamDto.setRepeatApiMap(updateApiModuleDTO.getRepeatApiMap());
        apiImportParamDto.setRepeatCaseList(updateApiModuleDTO.getRepeatCaseList());
        //处理导入数据与已存在数据关系
        if (request.getProtocol().equals("HTTP")) {
            if (urlRepeat) {
                repeatList = dealHttpUrlRepeat(apiImportParamDto);
            } else {
                repeatList = dealHttpUrlNoRepeat(apiImportParamDto);
            }
            if (optionData.isEmpty()) {
                moduleMap = new HashMap<>();
            }

        } else {
            repeatList = dealRepeat(apiImportParamDto);
        }

        Map<String, List<ApiTestCaseWithBLOBs>> apiIdCaseMap = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
        if (MapUtils.isNotEmpty(moduleMap)) {

            delModule(apiModuleService, optionData, moduleMap, apiModuleMapper);
        }
        int num = 0;
        if (!CollectionUtils.isEmpty(optionData) && optionData.get(0) != null && optionData.get(0).getProjectId() != null) {
            num = getNextNum(optionData.get(0).getProjectId(), extApiDefinitionMapper);
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
                if (urlRepeat) {
                    sameRefIds = repeatList.stream().filter(t -> t.getRefId().equals(item.getRefId()) && t.getModuleId().equals(item.getModuleId())).collect(Collectors.toList());
                } else {
                    sameRefIds = repeatList.stream().filter(t -> t.getRefId().equals(item.getRefId())).collect(Collectors.toList());
                }
                List<String> repeatIds = sameRefIds.stream().map(ApiDefinition::getId).toList();

                List<ApiDefinitionWithBLOBs> toUpdates = toUpdateList.stream().filter(t -> t.getRefId().equals(item.getRefId())).collect(Collectors.toList());
                List<String> toUpDateIds = toUpdates.stream().map(ApiDefinition::getId).toList();
                List<String> reduce1 = repeatIds.stream().filter(t -> !toUpDateIds.contains(t)).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(reduce1)) {
                    sameRefIds = toUpdates;
                }
            }
            if (StringUtils.equalsIgnoreCase(item.getVersionId(), "trash")) {
                if (CollectionUtils.isNotEmpty(sameRefIds)) {
                    for (ApiDefinitionWithBLOBs sameRefId : sameRefIds) {
                        batchMapper.updateByPrimaryKey(sameRefId);
                    }
                }
                continue;
            }
            List<ApiTestCaseWithBLOBs> caseList = apiIdCaseMap.get(item.getId());
            ApiDefinitionImportUtil.setModule(item, apiModuleMapper);
            if (item.getName().length() > 255) {
                item.setName(item.getName().substring(0, 255));
            }
            //如果是创建版本数据，则num和其他版本数据一致
            if (item.getVersionId() == null || (!item.getVersionId().equals("new") && !item.getVersionId().equals("update"))) {
                item.setNum(num++);
            }

            ApiDefinitionImportParamDTO apiDefinitionImportParam = new ApiDefinitionImportParamDTO(item, request, apiImport.getMocks(), toUpdateList, caseList);
            apiDefinitionImportParam.setRepeatList(sameRefIds);
            apiDefinitionImportParam.setImportType(request.getType());
            apiDefinitionImportParam.setScheduleId(request.getResourceId());
            apiDefinitionImportParam.setOperatingLogMapper(operatingLogMapper);
            apiDefinitionImportParam.setOperatingLogResourceMapper(operatingLogResourceMapper);
            ApiImportSendNoticeDTO apiImportSendNoticeDTO = importCreate(batchMapper, apiDefinitionImportParam);
            if (apiImportSendNoticeDTO != null) {
                apiImportSendNoticeDTOS.add(apiImportSendNoticeDTO);
            }
            if (i % 300 == 0) {
                sqlSession.flushStatements();
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

    private static void delModule(ApiModuleService apiModuleService, List<ApiDefinitionWithBLOBs> optionData, Map<String, ApiModule> moduleMap, ApiModuleMapper apiModuleMapper) {
        Map<String, ApiModule> rootModuleMap = new HashMap<>();
        Map<String, List<ApiDefinitionWithBLOBs>> moduleOptionData = optionData.stream().collect(Collectors.groupingBy(ApiDefinition::getModulePath));
        //过滤空的模块
        moduleMap.forEach((modulePath, v) -> {
            if (moduleOptionData.get(modulePath) != null && moduleOptionData.get(modulePath).size() > 0) {
                apiModuleMapper.insert(v);
            } else {
                rootModuleMap.put(modulePath, v);
            }
        });
        //防止空的模块是其余模块的父亲
        rootModuleMap.forEach((modulePath, v) -> {
            for (String path : moduleOptionData.keySet()) {
                String[] modulePathTree = apiModuleService.getPathTree(modulePath);
                String[] pathTree = apiModuleService.getPathTree(path);
                List<String> modulePathList = Arrays.asList(modulePathTree);
                List<String> pathTreeList = Arrays.asList(pathTree);
                if (new HashSet<>(pathTreeList).containsAll(modulePathList)) {
                    apiModuleMapper.insert(v);
                    break;
                }
            }
        });
    }

    /**
     * @param optionDatas     操作过可以导入的文件里的接口
     * @param repeatApiMap    文件里与可操作的接口重复的接口
     * @param repeatCaseList  文件里与可操作的case重复的case
     * @param optionDataCases 操作过可以导入的文件里的case
     */
    private void setRepeatApiToCase(List<ApiDefinitionWithBLOBs> optionDatas, Map<String, List<ApiDefinitionWithBLOBs>> repeatApiMap, List<ApiTestCaseWithBLOBs> repeatCaseList, List<ApiTestCaseWithBLOBs> optionDataCases) {
        Map<String, List<ApiTestCaseWithBLOBs>> apiIdCaseMap = repeatCaseList.stream().collect(Collectors.groupingBy(ApiTestCaseWithBLOBs::getApiDefinitionId));
        Map<String, List<ApiTestCaseWithBLOBs>> importCaseMap = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCaseWithBLOBs::getApiDefinitionId));
        for (ApiDefinitionWithBLOBs optionData : optionDatas) {
            //文件里和当前导入数据的接口重复的数据
            String apiId = optionData.getId();
            //文件重复的case,这里取出来是为了和api组成caseList导入进去
            List<ApiTestCaseWithBLOBs> apiTestCaseWithBLOBs = apiIdCaseMap.get(apiId);
            List<ApiDefinitionWithBLOBs> apiDefinitionWithBLOBs = repeatApiMap.get(apiId);
            if (CollectionUtils.isEmpty(apiTestCaseWithBLOBs)) {
                apiTestCaseWithBLOBs = new ArrayList<>();
            }
            if (CollectionUtils.isNotEmpty(apiDefinitionWithBLOBs)) {
                for (ApiDefinitionWithBLOBs apiDefinitionWithBLOB : apiDefinitionWithBLOBs) {
                    apiTestCaseWithBLOBs.add(apiToCase(apiDefinitionWithBLOB, apiId));
                }
            }
            //取出当前操作接口应该导入的case用于对比名字重复加序号
            List<String> nameList = new ArrayList<>();
            List<ApiTestCaseWithBLOBs> importCaseList = importCaseMap.get(apiId);
            if (CollectionUtils.isNotEmpty(importCaseList)) {
                nameList = importCaseList.stream().map(ApiTestCaseWithBLOBs::getName).collect(Collectors.toList());
            }
            for (int i = 0; i < apiTestCaseWithBLOBs.size(); i++) {
                ApiTestCaseWithBLOBs apiTestCaseWithBLOBs1 = apiTestCaseWithBLOBs.get(i);
                if (nameList.contains(apiTestCaseWithBLOBs1.getName())) {
                    apiTestCaseWithBLOBs1.setName(apiTestCaseWithBLOBs1.getName() + "0" + i);
                    nameList.add(apiTestCaseWithBLOBs1.getName() + "0" + i);
                } else {
                    nameList.add(apiTestCaseWithBLOBs1.getName());
                }
            }
            optionDataCases.addAll(apiTestCaseWithBLOBs);
        }

    }

    /**
     * 将接口转成用例
     *
     * @param apiDefinitionWithBLOB
     * @return
     */
    private ApiTestCaseWithBLOBs apiToCase(ApiDefinitionWithBLOBs apiDefinitionWithBLOB, String apiId) {
        ApiTestCaseWithBLOBs apiTestCase = new ApiTestCaseWithBLOBs();
        apiTestCase.setName(apiDefinitionWithBLOB.getName());
        apiTestCase.setApiDefinitionId(apiId);
        apiTestCase.setCreateUserId(apiDefinitionWithBLOB.getCreateUser());
        apiTestCase.setTags(apiDefinitionWithBLOB.getTags());
        apiTestCase.setStatus(apiDefinitionWithBLOB.getStatus());
        apiTestCase.setOriginalStatus(apiDefinitionWithBLOB.getOriginalState());
        apiTestCase.setVersionId(apiDefinitionWithBLOB.getVersionId());
        apiTestCase.setOrder(apiDefinitionWithBLOB.getOrder());
        apiTestCase.setCaseStatus(apiDefinitionWithBLOB.getCaseStatus());
        apiTestCase.setDescription(apiDefinitionWithBLOB.getDescription());
        apiTestCase.setRequest(apiDefinitionWithBLOB.getRequest());
        return apiTestCase;
    }

    private List<ApiDefinitionWithBLOBs> dealHttpUrlRepeat(ApiImportParamDto apiImportParamDto) {
        ApiTestImportRequest request = apiImportParamDto.getRequest();
        List<ApiDefinitionWithBLOBs> optionData = apiImportParamDto.getOptionData();
        ApiModuleDTO chooseModule = apiImportParamDto.getChooseModule();
        Map<String, String> moduleIdPathMap = apiImportParamDto.getIdPathMap();
        Boolean fullCoverage = apiImportParamDto.getFullCoverage();
        List<ApiDefinitionWithBLOBs> toUpdateList = apiImportParamDto.getToUpdateList();
        List<ApiTestCaseWithBLOBs> optionDataCases = apiImportParamDto.getOptionDataCases();
        Map<String, ApiModule> moduleMap = apiImportParamDto.getModuleMap();

        String updateVersionId = getUpdateVersionId(request);
        Boolean fullCoverageApi = getFullCoverageApi(request);
        String projectId = request.getProjectId();
        //系统内重复的数据
        if (StringUtils.isBlank(request.getModuleId())) {
            request.setModuleId(null);
        }
        List<ApiDefinitionWithBLOBs> repeatApiDefinitionWithBLOBs = extApiDefinitionMapper.selectRepeatByBLOBsSameUrl(optionData, projectId, null);

        //如果系统内，没有重复数据，要把文件重复的数据改成接口的case
        if (CollectionUtils.isEmpty(repeatApiDefinitionWithBLOBs)) {
            setRepeatApiToCase(optionData, apiImportParamDto.getRepeatApiMap(), apiImportParamDto.getRepeatCaseList(), optionDataCases);
        }
        //这个是名称加请求方式加路径加模块为key的map 就是为了去重
        Map<String, ApiDefinitionWithBLOBs> optionMap = new HashMap<>();
        //这个是系统内重复的数据
        Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap;
        //按照原来的顺序
        if (chooseModule != null) {
            //如果有选中的模块，则在选中的模块下过滤 过滤规则是 选择的模块路径+名称+method+path
            String chooseModuleParentId = getChooseModuleParentId(chooseModule);
            String chooseModulePath = getChooseModulePath(moduleIdPathMap, chooseModule, chooseModuleParentId);
            //这样的过滤规则下可能存在重复接口，如果是覆盖模块，需要按照去重规则再次去重，否则就加上接口原有的模块
            if (fullCoverage) {
                removeHttpChooseModuleRepeat(optionData, chooseModulePath);
            }
        }
        getUrlRepeatOptionMap(optionData, optionMap);
        repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.groupingBy(t -> t.getName()
                .concat(t.getMethod())
                .concat(t.getPath())
                .concat(StringUtils.isNotBlank(t.getModulePath()) ? t.getModulePath() : "/未规划用例")));
        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap = new HashMap<>();
        //重复接口的case
        if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
            oldCaseMap = ApiDefinitionImportUtil.getOldCaseMap(repeatApiDefinitionWithBLOBs, apiTestCaseMapper);
        }
        //覆盖接口
        if (fullCoverage) {
            //允许覆盖模块，用导入的重复数据的最后一条覆盖查询的所有重复数据; case 在覆盖的时候，是拼接到原来的case，name唯一；不覆盖，就用原来的
            if (fullCoverageApi) {
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    startCoverModule(toUpdateList, optionData, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap);
                }
            } else {
                //覆盖但不覆盖模块
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    //过滤同一层级重复模块，导入文件没有新增接口无需创建接口模块
                    moduleMap = judgeModuleMap(moduleMap, optionMap, repeatDataMap);
                    startCover(toUpdateList, optionData, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap);
                }
            }
        } else {
            //不覆盖,同一接口不做更新;可能创建新版本，case也直接创建，
            if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                removeSameData(repeatDataMap, optionMap, optionData, moduleMap, optionDataCases);
            }
        }
        //将原来的case和更改的case组合在一起，为了同步的设置
        List<String> caseIds = optionDataCases.stream().map(ApiTestCase::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        buildCases(optionDataCases, oldCaseMap, caseIds);
        return repeatApiDefinitionWithBLOBs;
    }

    private List<ApiDefinitionWithBLOBs> dealHttpUrlNoRepeat(ApiImportParamDto apiImportParamDto) {
        ApiTestImportRequest request = apiImportParamDto.getRequest();
        List<ApiDefinitionWithBLOBs> optionData = apiImportParamDto.getOptionData();
        Boolean fullCoverage = apiImportParamDto.getFullCoverage();
        List<ApiDefinitionWithBLOBs> toUpdateList = apiImportParamDto.getToUpdateList();
        List<ApiTestCaseWithBLOBs> optionDataCases = apiImportParamDto.getOptionDataCases();
        Map<String, ApiModule> moduleMap = apiImportParamDto.getModuleMap();

        //这个是名称加请求方式加路径加模块为key的map 就是为了去重
        Map<String, ApiDefinitionWithBLOBs> optionMap;

        String updateVersionId = getUpdateVersionId(request);
        Boolean fullCoverageApi = getFullCoverageApi(request);
        String projectId = request.getProjectId();
        //系统内重复的数据
        List<ApiDefinitionWithBLOBs> repeatApiDefinitionWithBLOBs = extApiDefinitionMapper.selectRepeatByBLOBs(optionData, projectId);
        //如果系统内，没有重复数据，要把文件重复的数据改成接口的case
        if (CollectionUtils.isEmpty(repeatApiDefinitionWithBLOBs)) {
            setRepeatApiToCase(optionData, apiImportParamDto.getRepeatApiMap(), apiImportParamDto.getRepeatCaseList(), optionDataCases);
        }
        //这个是系统内重复的数据
        Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.groupingBy(t -> t.getMethod().concat(t.getPath())));

        //按照原来的顺序
        optionMap = optionData.stream().collect(Collectors.toMap(t -> t.getMethod().concat(t.getPath()), api -> api));

        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap = new HashMap<>();

        //重复接口的case
        if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
            oldCaseMap = ApiDefinitionImportUtil.getOldCaseMap(repeatApiDefinitionWithBLOBs, apiTestCaseMapper);
        }

        if (fullCoverage) {
            if (fullCoverageApi) {
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    startCoverModule(toUpdateList, optionData, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap);
                }
            } else {
                //不覆盖模块
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    startCover(toUpdateList, optionData, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap);
                }
            }
        } else {
            //不覆盖,同一接口不做更新
            if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                removeSameData(repeatDataMap, optionMap, optionData, moduleMap, optionDataCases);
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

    private static void removeHttpChooseModuleRepeat(List<ApiDefinitionWithBLOBs> optionData, String chooseModulePath) {
        LinkedHashMap<String, List<ApiDefinitionWithBLOBs>> methodPathMap = optionData.stream().collect(Collectors.groupingBy(t -> t.getName().concat(t.getMethod()).concat(t.getPath()).concat(chooseModulePath), LinkedHashMap::new, Collectors.toList()));
        methodPathMap.forEach((k, v) -> {
            if (v.size() > 1) {
                for (int i = 0; i < v.size() - 1; i++) {
                    optionData.remove(v.get(i));
                }
            }
        });
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


    private static void startCoverModule(List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiDefinitionWithBLOBs> optionData,
                                         Map<String, ApiDefinitionWithBLOBs> methodPathMap, Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap,
                                         String updateVersionId, List<ApiTestCaseWithBLOBs> optionDataCases,
                                         Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap) {
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
                ApiDefinitionWithBLOBs versionApi = null;
                //v是根据重复条件筛选出来的重复接口集合
                for (ApiDefinitionWithBLOBs definitionWithBLOBs : v) {
                    //判断这里是否有最新的版本数据
                    if (definitionWithBLOBs.getLatest()) {
                        latestApi = definitionWithBLOBs;
                    }
                    if (!definitionWithBLOBs.getVersionId().equals(updateVersionId)) {
                        i += 1;
                        continue;
                    } else {
                        versionApi = definitionWithBLOBs;
                    }
                    //组合case
                    if (MapUtils.isNotEmpty(caseNameMap)) {
                        buildCaseList(oldCaseMap, caseNameMap, definitionWithBLOBs, optionDataCases);
                    }
                    //在指定版本中更新接口内容并变更接口模块为当前导入选择的模块下创建导入文件中接口指定的模块
                    ApiDefinitionWithBLOBs api = new ApiDefinitionWithBLOBs();
                    BeanUtils.copyBean(api, apiDefinitionWithBLOBs);
                    api.setId(definitionWithBLOBs.getId());
                    setApiParam(api, updateVersionId, definitionWithBLOBs);
                    coverApiList.add(api);
                }
                if (i == v.size()) {
                    //指定版本无数据
                    //如果系统内的所有版本都不是当前选择的数据更新版本，需要与lasted = 1 比较请求参数，参数一致，仅变更接口模块为当前导入接口的模块，不一致，新增并变更接口模块为当前导入接口的模块
                    if (latestApi != null) {
                        Boolean hasChange;
                        if (apiDefinitionWithBLOBs.getProtocol().equals("HTTP")) {
                            hasChange = ApiDefinitionImportUtil.checkIsSynchronize(latestApi, apiDefinitionWithBLOBs);
                        } else {
                            hasChange = true;
                        }
                        if (!hasChange) {
                            if (apiDefinitionWithBLOBs.getProtocol().equals("HTTP")) {
                                for (ApiDefinitionWithBLOBs definitionWithBLOBs : v) {
                                    definitionWithBLOBs.setModuleId(apiDefinitionWithBLOBs.getModuleId());
                                    definitionWithBLOBs.setModulePath(apiDefinitionWithBLOBs.getModulePath());
                                    definitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
                                    updateApiList.add(definitionWithBLOBs);
                                }
                            }
                            apiDefinitionWithBLOBs.setRefId(latestApi.getRefId());
                            apiDefinitionWithBLOBs.setVersionId("trash");
                        } else {
                            addNewVersionApi(apiDefinitionWithBLOBs, latestApi, "update");
                            if (apiDefinitionWithBLOBs.getProtocol().equals("HTTP")) {
                                for (ApiDefinitionWithBLOBs definitionWithBLOBs : v) {
                                    definitionWithBLOBs.setModuleId(apiDefinitionWithBLOBs.getModuleId());
                                    definitionWithBLOBs.setModulePath(apiDefinitionWithBLOBs.getModulePath());
                                    definitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
                                    updateApiList.add(definitionWithBLOBs);
                                }
                            }
                        }
                    }
                } else {
                    //指定版本有数据
                    if (apiDefinitionWithBLOBs.getProtocol().equals("HTTP")) {
                        //请求参数与指定版本的参数一致不做更新，原接口更新时间不变
                        if (versionApi != null) {
                            Boolean hasChange = ApiDefinitionImportUtil.checkIsSynchronize(versionApi, apiDefinitionWithBLOBs);
                            if (hasChange) {
                                for (ApiDefinitionWithBLOBs definitionWithBLOBs : v) {
                                    definitionWithBLOBs.setModuleId(apiDefinitionWithBLOBs.getModuleId());
                                    definitionWithBLOBs.setModulePath(apiDefinitionWithBLOBs.getModulePath());
                                    definitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
                                    updateApiList.add(definitionWithBLOBs);
                                }
                            } else {
                                updateApiList.addAll(v);
                            }
                        }

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
        List<String> intersection = repeatKeyList.stream().filter(importKeysList::contains).toList();
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

    private ApiImportSendNoticeDTO importCreate(ApiDefinitionMapper batchMapper, ApiDefinitionImportParamDTO apiDefinitionImportParamDTO) {
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
            return _importCreate(batchMapper, apiDefinitionImportParamDTO);
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
                    apiDefinition.setOrder(getImportNextOrder(apiTestImportRequest.getProjectId()));
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
                List<ApiTestCaseDTO> apiTestCaseDTOS = importCase(apiDefinition, caseList);
                apiImportSendNoticeDTO.setCaseDTOList(apiTestCaseDTOS);
                updateExitData(batchMapper, apiDefinitionImportParamDTO, apiDefinition);
            /*    extApiDefinitionMapper.clearLatestVersion(apiDefinition.getRefId());
                extApiDefinitionMapper.addLatestVersion(apiDefinition.getRefId());*/
                ApiDefinitionResult apiDefinitionResult = ApiDefinitionImportUtil.getApiDefinitionResult(apiDefinition, false);
                apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
                return apiImportSendNoticeDTO;
            } else {
                //不覆盖的接口，如果没有sameRequest则不导入。此时清空mock信息
                mocks.clear();
                return null;
            }
        } else {
            return _importCreate(batchMapper, apiDefinitionImportParamDTO);
        }
    }

    private static void updateExitData(ApiDefinitionMapper batchMapper, ApiDefinitionImportParamDTO apiDefinitionImportParamDTO, ApiDefinitionWithBLOBs apiDefinition) {
        List<ApiDefinitionWithBLOBs> repeatList = apiDefinitionImportParamDTO.getRepeatList();
        if (CollectionUtils.isEmpty(repeatList)) {
            return;
        }
        for (ApiDefinitionWithBLOBs apiDefinitionWithBLOBs : repeatList) {
            if (!apiDefinition.getId().equalsIgnoreCase(apiDefinitionWithBLOBs.getId())) {
                if (apiDefinition.getLatest() && apiDefinitionWithBLOBs.getLatest()) {
                    apiDefinitionWithBLOBs.setLatest(false);
                }
                batchMapper.updateByPrimaryKey(apiDefinitionWithBLOBs);
            }
        }
    }

    private List<ApiTestCaseDTO> importCase(ApiDefinitionWithBLOBs apiDefinition, List<ApiTestCaseWithBLOBs> caseList) {
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
                apiTestCaseWithBLOBs.setOrder(getImportNextCaseOrder(apiDefinition.getProjectId()));
            }
            if (apiTestCaseWithBLOBs.getNum() == null) {
                apiTestCaseWithBLOBs.setNum(apiTestCaseService.getNextNum(apiDefinition.getId(), apiDefinition.getNum() + i, apiDefinition.getProjectId()));
            }
            if (StringUtils.isBlank(apiTestCaseWithBLOBs.getProjectId())) {
                apiTestCaseWithBLOBs.setProjectId(apiDefinition.getProjectId());
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
                apiTestCaseWithBLOBs.setCreateTime(System.currentTimeMillis());
                BeanUtils.copyBean(apiTestCaseDTO, apiTestCaseWithBLOBs);
                apiTestCaseDTO.setUpdated(false);
                apiTestCaseMapper.insert(apiTestCaseWithBLOBs);
            }
            apiTestCaseDTOS.add(apiTestCaseDTO);
        }
        return apiTestCaseDTOS;
    }

    private List<ApiDefinitionWithBLOBs> dealRepeat(ApiImportParamDto apiImportParamDto) {
        ApiTestImportRequest request = apiImportParamDto.getRequest();
        List<ApiDefinitionWithBLOBs> optionData = apiImportParamDto.getOptionData();
        ApiModuleDTO chooseModule = apiImportParamDto.getChooseModule();
        Map<String, String> moduleIdPathMap = apiImportParamDto.getIdPathMap();
        Boolean fullCoverage = apiImportParamDto.getFullCoverage();
        List<ApiDefinitionWithBLOBs> toUpdateList = apiImportParamDto.getToUpdateList();
        List<ApiTestCaseWithBLOBs> optionDataCases = apiImportParamDto.getOptionDataCases();
        Map<String, ApiModule> moduleMap = apiImportParamDto.getModuleMap();

        String chooseModuleId = request.getModuleId();
        List<ApiDefinitionWithBLOBs> repeatApiDefinitionWithBLOBs = getApiDefinitionWithBLOBsList(request, optionData);
        //如果系统内，没有重复数据，要把文件重复的数据改成接口的case
        if (CollectionUtils.isEmpty(repeatApiDefinitionWithBLOBs)) {
            setRepeatApiToCase(optionData, apiImportParamDto.getRepeatApiMap(), apiImportParamDto.getRepeatCaseList(), optionDataCases);
        }
        //重复接口的case
        Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
            oldCaseMap = ApiDefinitionImportUtil.getOldCaseMap(repeatApiDefinitionWithBLOBs, apiTestCaseMapper);
        }
        Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap = null;
        Map<String, ApiDefinitionWithBLOBs> optionMap = new HashMap<>();

        if (chooseModule != null) {
            String chooseModuleParentId = getChooseModuleParentId(chooseModule);
            String chooseModulePath = getChooseModulePath(moduleIdPathMap, chooseModule, chooseModuleParentId);
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
        //处理数据
        if (fullCoverage) {
            if (fullCoverageApi) {
                startCoverModule(toUpdateList, optionData, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap);
            } else {
                //过滤同一层级重复模块，导入文件没有新增接口无需创建接口模块
                moduleMap = judgeModuleMap(moduleMap, optionMap, repeatDataMap);
                startCover(toUpdateList, optionData, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap);
            }
        } else {
            //不覆盖
            if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                removeSameData(repeatDataMap, optionMap, optionData, moduleMap, optionDataCases);
            }
        }

        //系统内检查重复
        if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
            repeatDataMap = repeatApiDefinitionWithBLOBs.stream().collect(Collectors.groupingBy(t -> t.getName().concat(t.getModulePath())));
            optionMap = optionData.stream().collect(Collectors.toMap(t -> t.getName().concat(t.getModulePath()), api -> api));
            if (fullCoverage) {
                startCover(toUpdateList, optionData, optionMap, repeatDataMap, updateVersionId, optionDataCases, oldCaseMap);
            } else {
                //不覆盖,同一接口不做更新
                if (CollectionUtils.isNotEmpty(repeatApiDefinitionWithBLOBs)) {
                    removeSameData(repeatDataMap, optionMap, optionData, moduleMap, optionDataCases);
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

    private static void getNoHChooseModuleUrlRepeatOptionMap(List<ApiDefinitionWithBLOBs> optionData, Map<String, ApiDefinitionWithBLOBs> optionMap, String chooseModulePath) {
        for (ApiDefinitionWithBLOBs optionDatum : optionData) {
            if (optionDatum.getModulePath() == null) {
                optionMap.put(optionDatum.getName().concat(chooseModulePath), optionDatum);
            } else {
                optionMap.put(optionDatum.getName().concat(chooseModulePath).concat(optionDatum.getModulePath()), optionDatum);
            }
        }
    }

    private List<ApiDefinitionWithBLOBs> getApiDefinitionWithBLOBsList(ApiTestImportRequest request, List<ApiDefinitionWithBLOBs> optionData) {
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
                                   Map<String, List<ApiTestCaseWithBLOBs>> oldCaseMap) {
        //要去覆盖接口的集合
        List<ApiDefinitionWithBLOBs> coverApiList = new ArrayList<>();
        //记录已存在数据可以被更新的集合
        List<ApiDefinitionWithBLOBs> updateApiList = new ArrayList<>();
        repeatDataMap.forEach((k, v) -> {
            ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = methodPathMap.get(k);
            if (apiDefinitionWithBLOBs != null) {
                //该接口的case
                Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
                Map<String, ApiTestCaseWithBLOBs> caseNameMap = getDistinctCaseNameMap(definitionIdCaseMAp, apiDefinitionWithBLOBs);
                int i = 0;
                //定义最新版本
                ApiDefinitionWithBLOBs latestApi = null;
                ApiDefinitionWithBLOBs versionApi = null;
                for (ApiDefinitionWithBLOBs definitionWithBLOBs : v) {
                    //判断这里是否有最新的版本数据
                    if (definitionWithBLOBs.getLatest()) {
                        latestApi = definitionWithBLOBs;
                    }
                    //为了记录指定版本是否有数据
                    if (!definitionWithBLOBs.getVersionId().equals(updateVersionId)) {
                        i += 1;
                        continue;
                    } else {
                        versionApi = definitionWithBLOBs;
                    }
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
                    //指定版本无数据
                    if (latestApi != null) {
                        boolean hasChange;
                        if (apiDefinitionWithBLOBs.getProtocol().equals("HTTP")) {
                            hasChange = ApiDefinitionImportUtil.checkIsSynchronize(latestApi, apiDefinitionWithBLOBs);
                        } else {
                            hasChange = true;
                        }
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
                    //指定版本有数据
                    if (apiDefinitionWithBLOBs.getProtocol().equals("HTTP")) {
                        //请求参数与指定版本的参数一致不做更新，原接口更新时间不变
                        if (versionApi != null) {
                            Boolean hasChange = ApiDefinitionImportUtil.checkIsSynchronize(versionApi, apiDefinitionWithBLOBs);
                            if (hasChange) {
                                for (ApiDefinitionWithBLOBs definitionWithBLOBs : v) {
                                    definitionWithBLOBs.setUpdateTime(System.currentTimeMillis());
                                    updateApiList.add(definitionWithBLOBs);
                                }
                            }
                        }
                    }
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

    private static void setApiParam(ApiDefinitionWithBLOBs apiDefinitionWithBLOBs, String versionId, ApiDefinitionWithBLOBs repeatApi) {
        apiDefinitionWithBLOBs.setVersionId(versionId);
        apiDefinitionWithBLOBs.setNum(repeatApi.getNum());
        apiDefinitionWithBLOBs.setStatus(repeatApi.getStatus());
        apiDefinitionWithBLOBs.setOrder(repeatApi.getOrder());
        apiDefinitionWithBLOBs.setRefId(repeatApi.getRefId());
        apiDefinitionWithBLOBs.setLatest(repeatApi.getLatest());
        apiDefinitionWithBLOBs.setCreateTime(repeatApi.getCreateTime());
        apiDefinitionWithBLOBs.setUpdateTime(repeatApi.getUpdateTime());
    }

    private static void buildOtherParam(List<ApiDefinitionWithBLOBs> toUpdateList, List<ApiDefinitionWithBLOBs> optionData, List<ApiDefinitionWithBLOBs> coverApiList, List<ApiDefinitionWithBLOBs> updateApiList) {
        optionData.addAll(coverApiList);
        toUpdateList.addAll(updateApiList);

    }

    private void removeSameData(Map<String, List<ApiDefinitionWithBLOBs>> repeatDataMap, Map<String, ApiDefinitionWithBLOBs> methodPathMap,
                                List<ApiDefinitionWithBLOBs> optionData, Map<String, ApiModule> moduleMap,
                                List<ApiTestCaseWithBLOBs> optionDataCases) {
        Map<String, ApiModule> parentIdModuleMap = new HashMap<>();
        for (ApiModule value : moduleMap.values()) {
            if (StringUtils.isBlank(value.getParentId())) {
                parentIdModuleMap.put("0", value);
            }
        }
        Map<String, List<ApiDefinitionWithBLOBs>> moduleOptionData = optionData.stream().collect(Collectors.groupingBy(ApiDefinition::getModulePath));
        repeatDataMap.forEach((k, v) -> {
            ApiDefinitionWithBLOBs apiDefinitionWithBLOBs = methodPathMap.get(k);
            if (apiDefinitionWithBLOBs != null) {
                Map<String, List<ApiTestCaseWithBLOBs>> definitionIdCaseMAp = optionDataCases.stream().collect(Collectors.groupingBy(ApiTestCase::getApiDefinitionId));
                List<ApiTestCaseWithBLOBs> distinctNameCases = definitionIdCaseMAp.get(apiDefinitionWithBLOBs.getId());
                String modulePath = apiDefinitionWithBLOBs.getModulePath();
                List<ApiDefinitionWithBLOBs> moduleData = moduleOptionData.get(modulePath);
                if (moduleData != null && moduleData.size() <= 1) {
                    ApiModule apiModule = moduleMap.get(modulePath);
                    if (apiModule != null && parentIdModuleMap.get(apiModule.getId()) == null) {
                        moduleMap.remove(modulePath);
                    }
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

    private ApiImportSendNoticeDTO _importCreate(ApiDefinitionMapper batchMapper, ApiDefinitionImportParamDTO apiDefinitionImportParamDTO) {
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
                apiDefinition.setOrder(getImportNextOrder(apiTestImportRequest.getProjectId()));
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
            ApiDefinitionResult apiDefinitionResult = ApiDefinitionImportUtil.getApiDefinitionResult(apiDefinition, false);
            apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
            List<ApiTestCaseDTO> apiTestCaseDTOS = importCase(apiDefinition, caseList);
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
                ApiDefinitionResult apiDefinitionResult = ApiDefinitionImportUtil.getApiDefinitionResult(apiDefinition, false);
                apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
                List<ApiTestCaseDTO> apiTestCaseDTOS = importCase(apiDefinition, caseList);
                apiImportSendNoticeDTO.setCaseDTOList(apiTestCaseDTOS);
            } else {
                ApiDefinitionWithBLOBs existApi = apiOp.get();
                apiDefinition.setStatus(existApi.getStatus());
                apiDefinition.setUpdateTime(existApi.getUpdateTime());
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
                if (!StringUtils.equalsIgnoreCase(apiTestImportRequest.getPlatform(), ApiImportPlatform.Metersphere.name())) {
                    apiDefinition.setTags(existApi.getTags()); // 其他格式 tag 不变，MS 格式替换
                }
                apiDefinition.setId(existApi.getId());
                setRequestAndAddNewCase(apiDefinition, caseList, false);
                apiDefinition.setOrder(existApi.getOrder());
                reSetImportMocksApiId(mocks, originId, apiDefinition.getId(), apiDefinition.getNum());
                batchMapper.updateByPrimaryKeyWithBLOBs(apiDefinition);
                //记录导入使得数据产生变更的操作记录
                setOperatingLog(existApi, apiDefinition, apiDefinitionImportParamDTO);
                ApiDefinitionResult apiDefinitionResult = ApiDefinitionImportUtil.getApiDefinitionResult(apiDefinition, true);
                apiImportSendNoticeDTO.setApiDefinitionResult(apiDefinitionResult);
                List<ApiTestCaseDTO> apiTestCaseDTOS = importCase(apiDefinition, caseList);
                apiImportSendNoticeDTO.setCaseDTOList(apiTestCaseDTOS);
            }
        }
        updateExitData(batchMapper, apiDefinitionImportParamDTO, apiDefinition);
        /*extApiDefinitionMapper.clearLatestVersion(apiDefinition.getRefId());
        extApiDefinitionMapper.addLatestVersion(apiDefinition.getRefId());*/
        return apiImportSendNoticeDTO;
    }

    private void setOperatingLog(ApiDefinitionWithBLOBs existApi, ApiDefinitionWithBLOBs apiDefinition, ApiDefinitionImportParamDTO apiDefinitionImportParamDTO) {
        OperatingLogWithBLOBs msOperLog = new OperatingLogWithBLOBs();
        msOperLog.setId(UUID.randomUUID().toString());
        msOperLog.setProjectId(existApi.getProjectId());
        msOperLog.setOperTitle(existApi.getName());
        msOperLog.setOperType(OperLogConstants.UPDATE.toString());
        setOperAndCreateUser(apiDefinitionImportParamDTO, msOperLog);
        msOperLog.setOperModule(OperLogModule.API_DEFINITION);
        msOperLog.setOperMethod("io.metersphere.api.controller.ApiDefinitionController.testCaseImport");
        msOperLog.setOperPath("/api/definition/import");
        OperatingLogDetails newDetails = getOperatingLogDetails(existApi, apiDefinition);
        msOperLog.setOperContent(JSON.toJSONString(newDetails));
        msOperLog.setCreateUser(newDetails.getCreateUser());
        msOperLog.setOperTime(System.currentTimeMillis());
        OperatingLogResource operatingLogResource = new OperatingLogResource();
        operatingLogResource.setOperatingLogId(msOperLog.getId());
        operatingLogResource.setId(UUID.randomUUID().toString());
        operatingLogResource.setSourceId(existApi.getId());
        apiDefinitionImportParamDTO.getOperatingLogResourceMapper().insert(operatingLogResource);
        apiDefinitionImportParamDTO.getOperatingLogMapper().insert(msOperLog);
    }

    private void setOperAndCreateUser(ApiDefinitionImportParamDTO apiDefinitionImportParamDTO, OperatingLogWithBLOBs msOperLog) {
        if (StringUtils.equals(apiDefinitionImportParamDTO.getImportType(), SCHEDULE)) {
            ScheduleExample schedule = new ScheduleExample();
            schedule.createCriteria().andResourceIdEqualTo(apiDefinitionImportParamDTO.getScheduleId());
            List<Schedule> list = scheduleMapper.selectByExample(schedule);
            if (list.size() > 0) {
                User user = baseUserService.getUserDTO(list.get(0).getUserId());
                msOperLog.setOperUser(user.getName() + Translator.get("timing_synchronization"));
                msOperLog.setCreateUser(user.getId());
            } else {
                msOperLog.setOperUser(Translator.get("timing_synchronization"));
            }
        } else {
            SessionUser user = SessionUtils.getUser();
            msOperLog.setOperUser(user.getName() + Translator.get("import_file"));
            msOperLog.setCreateUser(user.getId());
        }
    }

    @NotNull
    private static OperatingLogDetails getOperatingLogDetails(ApiDefinitionWithBLOBs existApi, ApiDefinitionWithBLOBs apiDefinition) {
        List<DetailColumn> oldColumns = ReflexObjectUtil.getColumns(existApi, DefinitionReference.definitionColumns);
        OperatingLogDetails oldDetails = new OperatingLogDetails(JSON.toJSONString(existApi.getId()), existApi.getProjectId(), existApi.getName(), existApi.getCreateUser(), oldColumns);
        List<DetailColumn> newColumns = ReflexObjectUtil.getColumns(apiDefinition, DefinitionReference.definitionColumns);
        OperatingLogDetails newDetails = new OperatingLogDetails(JSON.toJSONString(apiDefinition.getId()), apiDefinition.getProjectId(), apiDefinition.getName(), apiDefinition.getCreateUser(), newColumns);
        List<DetailColumn> columns = ReflexObjectUtil.compared(oldDetails, newDetails, OperLogModule.API_DEFINITION);
        newDetails.setColumns(columns);
        return newDetails;
    }


    private static List<ApiTestCaseWithBLOBs> setRequestAndAddNewCase(ApiDefinitionWithBLOBs apiDefinition, List<ApiTestCaseWithBLOBs> caseList, boolean newCreate) {
        boolean createCase = false;
        if (StringUtils.equalsIgnoreCase(apiDefinition.getProtocol(), RequestTypeConstants.HTTP)) {
            createCase = setImportHashTree(apiDefinition);
        } else if (StringUtils.equalsIgnoreCase(apiDefinition.getProtocol(), RequestTypeConstants.TCP)) {
            createCase = setImportTCPHashTree(apiDefinition);
        }
        if (newCreate && createCase && CollectionUtils.isEmpty(caseList)) {
            ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = ApiDefinitionImportUtil.addNewCase(apiDefinition);
            caseList = new ArrayList<>();
            caseList.add(apiTestCaseWithBLOBs);
        }
        return caseList;
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
        apiDefinition.setRequest(JSON.toJSONString(tcpSampler));
        return createCase;
    }

    public Long getImportNextOrder(String projectId) {
        Long order = currentApiOrder.get();
        if (order == null) {
            order = ServiceUtils.getNextOrder(projectId, extApiDefinitionMapper::getLastOrder);
        }
        order = order + ServiceUtils.ORDER_STEP;
        currentApiOrder.set(order);
        return order;
    }

    public Long getImportNextCaseOrder(String projectId) {
        Long order = currentApiCaseOrder.get();
        if (order == null) {
            order = ServiceUtils.getNextOrder(projectId, extApiTestCaseMapper::getLastOrder);
        }
        order = order + ServiceUtils.ORDER_STEP;
        currentApiCaseOrder.set(order);
        return order;
    }

}
