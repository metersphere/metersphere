package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.metersphere.api.dto.automation.*;
import io.metersphere.api.dto.dataCount.ApiDataCountResult;
import io.metersphere.api.dto.definition.RunDefinitionRequest;
import io.metersphere.api.dto.definition.request.*;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import io.metersphere.api.jmeter.JMeterService;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiTagMapper;
import io.metersphere.base.mapper.ext.ExtApiScenarioMapper;
import io.metersphere.base.mapper.ext.ExtTestPlanMapper;
import io.metersphere.commons.constants.APITestStatus;
import io.metersphere.commons.constants.ApiRunMode;
import io.metersphere.commons.constants.ReportTriggerMode;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.DateUtils;
import io.metersphere.commons.utils.ServiceUtils;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.i18n.Translator;
import io.metersphere.track.dto.TestPlanDTO;
import io.metersphere.track.request.testcase.QueryTestPlanRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiAutomationService {
    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiDefinitionService apiDefinitionService;
    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ApiTagMapper apiTagMapper;
    @Resource
    private JMeterService jMeterService;
    @Resource
    private ApiTestEnvironmentService environmentService;
    @Resource
    private ApiScenarioReportService apiReportService;
    @Resource
    private ExtTestPlanMapper extTestPlanMapper;
    @Resource
    SqlSessionFactory sqlSessionFactory;

    private static final String BODY_FILE_DIR = "/opt/metersphere/data/body";

    public List<ApiScenarioDTO> list(ApiScenarioRequest request) {
        request.setOrders(ServiceUtils.getDefaultOrder(request.getOrders()));
        List<ApiScenarioDTO> list = extApiScenarioMapper.list(request);
        ApiTagExample example = new ApiTagExample();
        example.createCriteria().andProjectIdEqualTo(request.getProjectId());
        List<ApiTag> tags = apiTagMapper.selectByExample(example);
        Map<String, String> tagMap = tags.stream().collect(Collectors.toMap(ApiTag::getId, ApiTag::getName));
        Gson gs = new Gson();
        list.forEach(item -> {
            if (item.getTagId() != null) {
                StringBuilder buf = new StringBuilder();
                gs.fromJson(item.getTagId(), List.class).forEach(t -> {
                    buf.append(tagMap.get(t));
                    buf.append(",");
                });
                if (buf != null && buf.length() > 0) {
                    String tagNames = buf.toString().substring(0, buf.toString().length() - 1);
                    List<String> tagList = Arrays.asList(tagNames.split(","));
                    item.setTagNames(tagList);
                } else {
                    item.setTagNames(new ArrayList<>());
                }
            }
        });
        return list;
    }

    public void deleteByIds(List<String> nodeIds) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andApiScenarioModuleIdIn(nodeIds);
        apiScenarioMapper.deleteByExample(example);
    }

    public ApiScenario create(SaveApiScenarioRequest request, List<MultipartFile> bodyFiles) {
        request.setId(UUID.randomUUID().toString());
        checkNameExist(request);

        final ApiScenarioWithBLOBs scenario = new ApiScenarioWithBLOBs();
        scenario.setId(request.getId());
        scenario.setName(request.getName());
        scenario.setProjectId(request.getProjectId());
        scenario.setTagId(request.getTagId());
        scenario.setApiScenarioModuleId(request.getApiScenarioModuleId());
        scenario.setModulePath(request.getModulePath());
        scenario.setLevel(request.getLevel());
        scenario.setFollowPeople(request.getFollowPeople());
        scenario.setPrincipal(request.getPrincipal());
        scenario.setStepTotal(request.getStepTotal());
        scenario.setScenarioDefinition(JSON.toJSONString(request.getScenarioDefinition()));
        scenario.setCreateTime(System.currentTimeMillis());
        scenario.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isNotEmpty(request.getStatus())) {
            scenario.setStatus(request.getStatus());
        } else {
            scenario.setStatus(ScenarioStatus.Underway.name());
        }
        if (request.getUserId() == null) {
            scenario.setUserId(SessionUtils.getUserId());
        } else {
            scenario.setUserId(request.getUserId());
        }
        scenario.setDescription(request.getDescription());
        apiScenarioMapper.insert(scenario);

        List<String> bodyUploadIds = request.getBodyUploadIds();
        apiDefinitionService.createBodyFiles(bodyUploadIds, bodyFiles);
        return scenario;
    }

    public void update(SaveApiScenarioRequest request, List<MultipartFile> bodyFiles) {
        checkNameExist(request);
        List<String> bodyUploadIds = request.getBodyUploadIds();
        apiDefinitionService.createBodyFiles(bodyUploadIds, bodyFiles);

        final ApiScenarioWithBLOBs scenario = new ApiScenarioWithBLOBs();
        scenario.setId(request.getId());
        scenario.setName(request.getName());
        scenario.setProjectId(request.getProjectId());
        scenario.setTagId(request.getTagId());
        scenario.setApiScenarioModuleId(request.getApiScenarioModuleId());
        scenario.setModulePath(request.getModulePath());
        scenario.setLevel(request.getLevel());
        scenario.setFollowPeople(request.getFollowPeople());
        scenario.setPrincipal(request.getPrincipal());
        scenario.setStepTotal(request.getStepTotal());
        scenario.setScenarioDefinition(JSON.toJSONString(request.getScenarioDefinition()));
        scenario.setUpdateTime(System.currentTimeMillis());
        if (StringUtils.isNotEmpty(request.getStatus())) {
            scenario.setStatus(request.getStatus());
        } else {
            scenario.setStatus(ScenarioStatus.Underway.name());
        }
        scenario.setUserId(request.getUserId());
        scenario.setDescription(request.getDescription());
        apiScenarioMapper.updateByPrimaryKeySelective(scenario);
    }

    public void delete(String id) {
        apiScenarioMapper.deleteByPrimaryKey(id);
    }

    public void deleteBatch(List<String> ids) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andIdIn(ids);
        apiScenarioMapper.deleteByExample(example);
    }

    public void removeToGc(List<String> apiIds) {
        extApiScenarioMapper.removeToGc(apiIds);
    }

    public void reduction(List<SaveApiScenarioRequest> requests) {
        List<String> apiIds = new ArrayList<>();
        requests.forEach(item -> {
            checkNameExist(item);
            apiIds.add(item.getId());
        });
        extApiScenarioMapper.reduction(apiIds);
    }

    private void checkNameExist(SaveApiScenarioRequest request) {
        ApiScenarioExample example = new ApiScenarioExample();
        example.createCriteria().andNameEqualTo(request.getName()).andProjectIdEqualTo(request.getProjectId()).andStatusNotEqualTo("Trash").andIdNotEqualTo(request.getId());
        if (apiScenarioMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("automation_name_already_exists"));
        }
    }

    public ApiScenarioWithBLOBs getApiScenario(String id) {
        return apiScenarioMapper.selectByPrimaryKey(id);
    }

    public List<ApiScenarioWithBLOBs> getApiScenarios(List<String> ids) {
        if (CollectionUtils.isNotEmpty(ids)) {
            return extApiScenarioMapper.selectIds(ids);
        }
        return new ArrayList<>();
    }

    public void deleteTag(String id) {
        List<ApiScenarioWithBLOBs> list = extApiScenarioMapper.selectByTagId(id);
        if (!list.isEmpty()) {
            Gson gs = new Gson();
            list.forEach(item -> {
                List<String> tagIds = gs.fromJson(item.getTagId(), List.class);
                tagIds.remove(id);
                item.setTagId(JSON.toJSONString(tagIds));
                apiScenarioMapper.updateByPrimaryKeySelective(item);
            });
        }
    }

    private void createAPIScenarioReportResult(String id, String triggerMode, String execType, String projectId) {
        APIScenarioReportResult report = new APIScenarioReportResult();
        report.setId(id);
        report.setName("测试执行结果");
        report.setCreateTime(System.currentTimeMillis());
        report.setUpdateTime(System.currentTimeMillis());
        report.setStatus(APITestStatus.Running.name());
        report.setUserId(SessionUtils.getUserId());
        report.setTriggerMode(triggerMode);
        report.setExecuteType(execType);
        report.setProjectId(projectId);
        apiReportService.addResult(report);

    }

    /**
     * 场景测试执行
     *
     * @param request
     * @return
     */
    public String run(RunScenarioRequest request) {
        List<ApiScenarioWithBLOBs> apiScenarios = extApiScenarioMapper.selectIds(request.getScenarioIds());
        MsTestPlan testPlan = new MsTestPlan();
        testPlan.setHashTree(new LinkedList<>());
        HashTree jmeterTestPlanHashTree = new ListedHashTree();
        for (ApiScenarioWithBLOBs item : apiScenarios) {
            MsThreadGroup group = new MsThreadGroup();
            group.setLabel(item.getName());
            group.setName(item.getName());
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                JSONObject element = JSON.parseObject(item.getScenarioDefinition());
                MsScenario scenario = JSONObject.parseObject(item.getScenarioDefinition(), MsScenario.class);
                // 多态JSON普通转换会丢失内容，需要通过 ObjectMapper 获取
                if (element != null && StringUtils.isNotEmpty(element.getString("hashTree"))) {
                    LinkedList<MsTestElement> elements = mapper.readValue(element.getString("hashTree"),
                            new TypeReference<LinkedList<MsTestElement>>() {
                            });
                    scenario.setHashTree(elements);
                }
                if (StringUtils.isNotEmpty(element.getString("variables"))) {
                    LinkedList<KeyValue> variables = mapper.readValue(element.getString("variables"),
                            new TypeReference<LinkedList<KeyValue>>() {
                            });
                    scenario.setVariables(variables);
                }
                LinkedList<MsTestElement> scenarios = new LinkedList<>();
                scenarios.add(scenario);
                group.setHashTree(scenarios);
                testPlan.getHashTree().add(group);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        testPlan.toHashTree(jmeterTestPlanHashTree, testPlan.getHashTree(), new ParameterConfig());
        // 调用执行方法
        jMeterService.runDefinition(request.getId(), jmeterTestPlanHashTree, request.getReportId(), ApiRunMode.SCENARIO.name());

        createAPIScenarioReportResult(request.getId(), request.getTriggerMode() == null ? ReportTriggerMode.MANUAL.name() : request.getTriggerMode(),
                request.getExecuteType(), request.getProjectId());
        return request.getId();
    }


    /**
     * 场景测试执行
     *
     * @param request
     * @param bodyFiles
     * @return
     */
    public String run(RunDefinitionRequest request, List<MultipartFile> bodyFiles) {
        List<String> bodyUploadIds = new ArrayList<>(request.getBodyUploadIds());
        apiDefinitionService.createBodyFiles(bodyUploadIds, bodyFiles);
        EnvironmentConfig envConfig = null;
        if (request.getEnvironmentId() != null) {
            ApiTestEnvironmentWithBLOBs environment = environmentService.get(request.getEnvironmentId());
            envConfig = JSONObject.parseObject(environment.getConfig(), EnvironmentConfig.class);
        }
        ParameterConfig config = new ParameterConfig();
        config.setConfig(envConfig);
        HashTree hashTree = request.getTestElement().generateHashTree(config);
        // 调用执行方法
        jMeterService.runDefinition(request.getId(), hashTree, request.getReportId(), ApiRunMode.SCENARIO.name());
        createAPIScenarioReportResult(request.getId(), ReportTriggerMode.MANUAL.name(), request.getExecuteType(), request.getProjectId());
        return request.getId();
    }

    public ReferenceDTO getReference(ApiScenarioRequest request) {
        ReferenceDTO dto = new ReferenceDTO();
        dto.setScenarioList(extApiScenarioMapper.selectReference(request));
        QueryTestPlanRequest planRequest = new QueryTestPlanRequest();
        planRequest.setScenarioId(request.getId());
        planRequest.setProjectId(request.getProjectId());
        dto.setTestPlanList(extTestPlanMapper.selectReference(planRequest));
        return dto;
    }

    public String addScenarioToPlan(SaveApiPlanRequest request) {
        if (CollectionUtils.isEmpty(request.getPlanIds())) {
            MSException.throwException(Translator.get("plan id is null "));
        }
        List<TestPlanDTO> list = extTestPlanMapper.selectByIds(request.getPlanIds());
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        ExtTestPlanMapper mapper = sqlSession.getMapper(ExtTestPlanMapper.class);
        list.forEach(item -> {
            if (CollectionUtils.isNotEmpty(request.getApiIds())) {
                if (CollectionUtils.isNotEmpty(request.getApiIds())) {
                    if (StringUtils.isEmpty(item.getApiIds())) {
                        item.setApiIds(JSON.toJSONString(request.getApiIds()));
                    } else {
                        // 合并api
                        List<String> dbApiIDs = JSON.parseArray(item.getApiIds(), String.class);
                        List<String> result = Stream.of(request.getApiIds(), dbApiIDs)
                                .flatMap(Collection::stream).distinct().collect(Collectors.toList());
                        item.setApiIds(JSON.toJSONString(result));
                    }
                    item.setScenarioIds(null);
                }
            }
            if (CollectionUtils.isNotEmpty(request.getScenarioIds())) {
                if (CollectionUtils.isNotEmpty(request.getScenarioIds())) {
                    if (StringUtils.isEmpty(item.getScenarioIds())) {
                        item.setScenarioIds(JSON.toJSONString(request.getScenarioIds()));
                    } else {
                        // 合并场景ID
                        List<String> dbScenarioIDs = JSON.parseArray(item.getScenarioIds(), String.class);
                        List<String> result = Stream.of(request.getScenarioIds(), dbScenarioIDs)
                                .flatMap(Collection::stream).distinct().collect(Collectors.toList());
                        item.setScenarioIds(JSON.toJSONString(result));
                    }
                    item.setApiIds(null);
                }
            }
            mapper.updatePlan(item);
        });
        sqlSession.flushStatements();
        return "success";
    }

    public long countScenarioByProjectID(String projectId) {
        return extApiScenarioMapper.countByProjectID(projectId);
    }

    public long countScenarioByProjectIDAndCreatInThisWeek(String projectId) {
        Map<String, Date> startAndEndDateInWeek = DateUtils.getWeedFirstTimeAndLastTime(new Date());

        Date firstTime = startAndEndDateInWeek.get("firstTime");
        Date lastTime = startAndEndDateInWeek.get("lastTime");

        if (firstTime == null || lastTime == null) {
            return 0;
        } else {
            return extApiScenarioMapper.countByProjectIDAndCreatInThisWeek(projectId, firstTime.getTime(), lastTime.getTime());
        }
    }

    public List<ApiDataCountResult> countRunResultByProjectID(String projectId) {
        return extApiScenarioMapper.countRunResultByProjectID(projectId);
    }
}
