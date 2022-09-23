package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import io.metersphere.api.dto.ApiScenarioReportBaseInfoDTO;
import io.metersphere.api.dto.ApiScenarioReportDTO;
import io.metersphere.api.dto.RequestResultExpandDTO;
import io.metersphere.api.dto.StepTreeDTO;
import io.metersphere.api.exec.scenario.ApiScenarioEnvService;
import io.metersphere.api.exec.utils.ResultParseUtil;
import io.metersphere.api.service.vo.ApiDefinitionExecResultVo;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.base.mapper.ext.ExtApiScenarioReportResultMapper;
import io.metersphere.commons.constants.CommandType;
import io.metersphere.commons.constants.ExecuteResult;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.constants.ReportTypeConstants;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.constants.SystemConstants;
import io.metersphere.dto.RequestResult;
import io.metersphere.dto.UiCommandResult;
import io.metersphere.service.ProjectService;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioReportStructureService {
    private static final List<String> REQUESTS = Arrays.asList("HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler", "JSR223Processor", "AbstractSampler");
    private static final List<String> CONTROLS = Arrays.asList("Assertions", "IfController", "ConstantTimer");
    private static final String RESOURCE_ID = "resourceId";
    private static final String REFERENCED = "referenced";
    private static final String ERROR_CODE = "errorCode";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String SCENARIO = "scenario";
    private static final String TYPE = "type";
    private static final String HASH_TREE = "hashTree";
    private static final String ENABLE = "enable";
    private static final String UI_COMMAND = "MsUiCommand";
    private static final String ERROR_REPORT = "errorReportResult";
    private static final String ERROR = "Error";

    @Resource
    private ApiScenarioReportStructureMapper mapper;
    @Resource
    private ApiScenarioReportResultMapper reportResultMapper;
    @Resource
    private ApiScenarioReportMapper scenarioReportMapper;
    @Resource
    private ApiDefinitionExecResultMapper definitionExecResultMapper;
    @Resource
    private ExtApiScenarioReportResultMapper extApiScenarioReportResultMapper;
    @Lazy
    @Resource
    private ProjectService projectService;
    @Resource
    private ApiTestEnvironmentService apiTestEnvironmentService;
    @Lazy
    @Resource
    private ApiScenarioEnvService apiScenarioEnvService;
    @Resource
    private ApiScenarioReportMapper apiScenarioReportMapper;

    public void save(List<ApiScenarioWithBLOBs> apiScenarios, String reportId, String reportType) {
        List<StepTreeDTO> dtoList = new LinkedList<>();
        for (ApiScenarioWithBLOBs bos : apiScenarios) {
            StepTreeDTO dto = dataFormatting(bos, reportType);
            dtoList.add(dto);
        }
        this.save(reportId, dtoList);
    }

    public List<StepTreeDTO> get(List<ApiScenarioWithBLOBs> apiScenarios, String reportType) {
        List<StepTreeDTO> dtoList = new LinkedList<>();
        for (ApiScenarioWithBLOBs bos : apiScenarios) {
            StepTreeDTO dto = dataFormatting(bos, reportType);
            dtoList.add(dto);
        }
        return dtoList;
    }

    public void saveUi(List<UiScenarioWithBLOBs> uiScenarios, String reportId, String reportType) {
        List<StepTreeDTO> dtoList = new LinkedList<>();
        for (UiScenarioWithBLOBs bos : uiScenarios) {
            StepTreeDTO dto = dataFormatting(bos, reportType);
            dtoList.add(dto);
        }
        if (LoggerUtil.getLogger().isDebugEnabled()) {
            LoggerUtil.debug("UI Scenario run-执行脚本装载-生成场景报告结构：" + JSON.toJSONString(dtoList));
        }
        this.save(reportId, dtoList);
    }

    public void save(ApiScenarioWithBLOBs apiScenario, String reportId, String reportType) {
        List<StepTreeDTO> dtoList = new LinkedList<>();
        StepTreeDTO dto = dataFormatting(apiScenario, reportType);
        dtoList.add(dto);
        this.save(reportId, dtoList);
    }

    public void save(UiScenarioWithBLOBs uiScenario, String reportId, String reportType) {
        List<StepTreeDTO> dtoList = new LinkedList<>();
        StepTreeDTO dto = dataFormatting(uiScenario, reportType);
        dtoList.add(dto);
        this.save(reportId, dtoList);
    }

    public void save(String reportId, List<StepTreeDTO> dtoList) {
        ApiScenarioReportStructureWithBLOBs structure = new ApiScenarioReportStructureWithBLOBs();
        structure.setId(UUID.randomUUID().toString());
        structure.setCreateTime(System.currentTimeMillis());
        structure.setReportId(reportId);
        structure.setResourceTree(JSON.toJSONString(dtoList).getBytes(StandardCharsets.UTF_8));
        mapper.insert(structure);
    }

    public void update(String reportId, List<StepTreeDTO> dtoList) {
        ApiScenarioReportStructureExample example = new ApiScenarioReportStructureExample();
        example.createCriteria().andReportIdEqualTo(reportId);
        List<ApiScenarioReportStructureWithBLOBs> structures = mapper.selectByExampleWithBLOBs(example);
        if (CollectionUtils.isNotEmpty(structures)) {
            ApiScenarioReportStructureWithBLOBs structure = structures.get(0);
            structure.setResourceTree(JSON.toJSONString(dtoList).getBytes(StandardCharsets.UTF_8));
            mapper.updateByPrimaryKeySelective(structure);
        }
    }

    public void update(String reportId, String console) {
        ApiScenarioReportStructureExample example = new ApiScenarioReportStructureExample();
        example.createCriteria().andReportIdEqualTo(reportId);
        List<ApiScenarioReportStructureWithBLOBs> structures = mapper.selectByExampleWithBLOBs(example);
        for (ApiScenarioReportStructureWithBLOBs structure : structures) {
            structure.setConsole(console);
            mapper.updateByPrimaryKeyWithBLOBs(structure);
        }
    }

    public static StepTreeDTO dataFormatting(ApiScenarioWithBLOBs apiScenario, String reportType) {
        return dataFormatting(apiScenario.getId(), apiScenario.getName(), apiScenario.getScenarioDefinition(), reportType);
    }

    public static StepTreeDTO dataFormatting(UiScenarioWithBLOBs uiScenario, String reportType) {
        return dataFormatting(null, uiScenario.getName(), uiScenario.getScenarioDefinition(), reportType);
    }

    private static String combinationResourceId(JSONObject element, String reportType, String id) {
        element = getRefElement(element);
        String resourceId = StringUtils.isNotEmpty(element.getString(ID))
                ? element.getString(ID) : element.getString(RESOURCE_ID);
        if (StringUtils.equals(reportType, RunModeConstants.SET_REPORT.toString())) {
            if (StringUtils.equals(element.getString(TYPE), SCENARIO)) {
                resourceId = id;
            } else if (StringUtils.isNotEmpty(resourceId) && StringUtils.isNotEmpty(id) && !resourceId.contains(id)) {
                resourceId = id + "=" + resourceId;
            }
        }
        return resourceId;
    }

    public static StepTreeDTO dataFormatting(String id, String name, String scenarioDefinition, String reportType) {
        JSONObject element = JSON.parseObject(scenarioDefinition, Feature.DisableSpecialKeyDetect);
        if (element != null && element.getBoolean(ENABLE)) {
            String resourceId = combinationResourceId(element, reportType, id);
            StepTreeDTO dto = new StepTreeDTO(name, resourceId, element.getString(TYPE), resourceId, 1);
            dto.setAllIndex(null);
            if (element.containsKey(HASH_TREE) && !REQUESTS.contains(dto.getType())) {
                JSONArray elementJSONArray = element.getJSONArray(HASH_TREE);
                dataFormatting(elementJSONArray, dto, id, reportType);
            }
            return dto;
        }
        return null;
    }

    private static JSONObject getRefElement(JSONObject element) {
        String referenced = element.getString(REFERENCED);
        if (StringUtils.equals(referenced, MsTestElementConstants.REF.name())) {
            if (StringUtils.equals(element.getString(TYPE), SCENARIO)) {
                ApiScenarioWithBLOBs scenarioWithBLOBs = CommonBeanFactory.getBean(ApiScenarioMapper.class).selectByPrimaryKey(element.getString(ID));
                if (scenarioWithBLOBs != null) {
                    return JSON.parseObject(scenarioWithBLOBs.getScenarioDefinition(), Feature.DisableSpecialKeyDetect);
                }
            }
        }
        return element;
    }

    public static void dataFormatting(JSONArray hashTree, StepTreeDTO dto, String id, String reportType) {
        for (int i = 0; i < hashTree.size(); i++) {
            JSONObject element = hashTree.getJSONObject(i);
            if (element != null && element.getBoolean(ENABLE)) {
                String resourceId = combinationResourceId(element, reportType, id);
                StepTreeDTO children = new StepTreeDTO(element.getString(NAME), resourceId, element.getString(TYPE), resourceId, element.getIntValue("index"));
                if (StringUtils.isNotBlank(children.getType()) && children.getType().equals(UI_COMMAND)) {
                    children.setResourceId(resourceId);
                    children.setLabel(element.getString(NAME));
                    children.setCmdType(element.getString("commandType"));
                } else if (StringUtils.isNotEmpty(dto.getAllIndex())) {
                    children.setAllIndex(dto.getAllIndex() + "_" + (children.getIndex() == 0 ? (i + 1) : children.getIndex()));
                    children.setResourceId(resourceId + "_" + children.getAllIndex());
                } else {
                    children.setAllIndex("" + (children.getIndex() == 0 ? (i + 1) : children.getIndex()));
                    children.setResourceId(resourceId + "_" + children.getAllIndex());
                }
                dto.getChildren().add(children);
                if (element.containsKey(HASH_TREE) && !REQUESTS.contains(children.getType())) {
                    JSONArray elementJSONArray = element.getJSONArray(HASH_TREE);
                    dataFormatting(elementJSONArray, children, id, reportType);
                }
            }
        }
    }

    private void calculateScenarios(List<StepTreeDTO> dtoList,
                                    AtomicLong totalScenario, AtomicLong scenarioError, AtomicLong errorReport, AtomicLong unExecute) {
        for (StepTreeDTO step : dtoList) {
            totalScenario.set(totalScenario.longValue() + 1);
            if (StringUtils.equalsIgnoreCase(step.getTotalStatus(), ExecuteResult.FAIL.getValue())) {
                scenarioError.set(scenarioError.longValue() + 1);
            } else if (StringUtils.equalsAnyIgnoreCase(step.getTotalStatus(), ERROR_CODE, ExecuteResult.ERROR_REPORT_RESULT.toString())) {
                errorReport.set(errorReport.longValue() + 1);
            } else if (!StringUtils.equalsIgnoreCase(step.getTotalStatus(), ExecuteResult.API_SUCCESS.getValue())) {
                unExecute.set(unExecute.longValue() + 1);
            }
        }
    }

    private void calculate(List<StepTreeDTO> dtoList, AtomicLong totalTime) {
        for (StepTreeDTO step : dtoList) {
            if (!StringUtils.equalsAny(step.getType(), SCENARIO, "UiScenario") && step.getValue() != null) {
                if (step.getValue().getStartTime() == 0 || step.getValue().getEndTime() == 0) {
                    totalTime.set(totalTime.longValue() + 0);
                } else if (step.getValue().getStartTime() > step.getValue().getEndTime() && step.getValue().getResponseResult() != null) {
                    // 异常时间处理
                    totalTime.set(totalTime.longValue() + step.getValue().getResponseResult().getResponseTime());
                } else {
                    totalTime.set((totalTime.longValue() + (step.getValue().getEndTime() - step.getValue().getStartTime())));
                }
            }
            if (CollectionUtils.isNotEmpty(step.getChildren())) {
                calculate(step.getChildren(), totalTime);
            }
        }
    }

    private void calculateStep(List<StepTreeDTO> dtoList, AtomicLong stepTotal, AtomicLong stepError, AtomicLong stepErrorCode, AtomicLong stepUnExecute) {
        for (StepTreeDTO root : dtoList) {
            int unExecSize = 0;
            if (CollectionUtils.isNotEmpty(root.getChildren())) {
                stepTotal.set((stepTotal.longValue() + root.getChildren().size()));
                for (StepTreeDTO step : root.getChildren()) {
                    if (StringUtils.equalsAnyIgnoreCase(step.getTotalStatus(), ExecuteResult.FAIL.getValue(), ERROR)) {
                        stepError.set(stepError.longValue() + 1);
                    } else if (StringUtils.equalsAnyIgnoreCase(step.getTotalStatus(), ERROR_CODE, ERROR_REPORT)) {
                        stepErrorCode.set(stepErrorCode.longValue() + 1);
                    } else if (!StringUtils.equalsIgnoreCase(step.getTotalStatus(), ExecuteResult.API_SUCCESS.getValue())) {
                        stepUnExecute.set(stepUnExecute.longValue() + 1);
                        unExecSize++;
                    }
                }
            }
            root.setUnExecuteTotal(unExecSize);
        }
    }

    public void reportFormatting(List<StepTreeDTO> dtoList, Map<String, List<ApiScenarioReportResultWithBLOBs>> maps, String reportType) {
        // 按照创建时间排序
        for (int index = 0; index < dtoList.size(); index++) {
            StepTreeDTO dto = dtoList.get(index);
            dto.setIndex((index + 1));
            List<ApiScenarioReportResultWithBLOBs> reportResults = maps.get(dto.getResourceId());
            if (CollectionUtils.isNotEmpty(reportResults)) {
                for (int i = 0; i < reportResults.size(); i++) {
                    ApiScenarioReportResultWithBLOBs reportResult = reportResults.get(i);
                    StepTreeDTO step = i == 0 ? dto :
                            new StepTreeDTO(dto.getLabel(), UUID.randomUUID().toString(), dto.getType(), reportResults.get(i).getId(), (i + 1));
                    step.setStepId(reportResults.get(i).getId());
                    RequestResult result = new RequestResultExpandDTO(reportResult);
                    if (reportResult.getContent() != null) {
                        if (reportType.startsWith(SystemConstants.TestTypeEnum.UI.name())) {
                            result = getUIRequestResult(reportResults, i, result);
                        } else {
                            result = JSON.parseObject(new String(reportResults.get(i).getContent(), StandardCharsets.UTF_8), RequestResult.class);
                        }
                    }
                    step.setValue(result);
                    step.setTotalStatus(reportResult.getStatus());
                    step.setErrorCode(reportResults.get(i).getErrorCode());
                    if (i > 0) {
                        dtoList.add(step);
                    }
                }
            }
            // 未执行请求
            if (StringUtils.isNotEmpty(dto.getType()) && REQUESTS.contains(dto.getType()) && dto.getValue() == null || isUiUnExecuteCommand(dto)) {
                dto.setTotalStatus(ExecuteResult.UN_EXECUTE.getValue());
                dto.setValue(new RequestResultExpandDTO(dto.getLabel(), ExecuteResult.UN_EXECUTE.getValue()));
            } else if (StringUtils.isNotEmpty(dto.getType()) && CONTROLS.contains(dto.getType()) && dto.getValue() == null) {
                // 条件控制步骤
                dto.setTotalStatus(ExecuteResult.API_SUCCESS.getValue());
                dto.setValue(new RequestResultExpandDTO(dto.getLabel(), ExecuteResult.API_SUCCESS.getValue()));
            } else if (dto.getValue() instanceof RequestResultExpandDTO && StringUtils.isNotEmpty(((RequestResultExpandDTO) dto.getValue()).getStatus())) {
                dto.setTotalStatus(((RequestResultExpandDTO) dto.getValue()).getStatus());
            } else if (dto.getValue() != null && StringUtils.isEmpty(dto.getTotalStatus())) {
                if (dto.getValue().getError() > 0 || BooleanUtils.isNotTrue(dto.getValue().isSuccess())) {
                    dto.setTotalStatus(ExecuteResult.FAIL.getValue());
                } else {
                    dto.setTotalStatus(ExecuteResult.API_SUCCESS.getValue());
                }
            }
            if (StringUtils.isNotEmpty(dto.getErrorCode()) && StringUtils.isEmpty(dto.getTotalStatus())) {
                dto.setTotalStatus(ERROR_CODE);
            }

            if (CollectionUtils.isNotEmpty(dto.getChildren())) {
                reportFormatting(dto.getChildren(), maps, reportType);

                if (StringUtils.isEmpty(dto.getErrorCode())) {
                    //统计child的errorCode，合并到parent中
                    List<String> childErrorCodeList = new ArrayList<>();
                    for (StepTreeDTO child : dto.getChildren()) {
                        if (StringUtils.isNotEmpty(child.getErrorCode()) && !childErrorCodeList.contains(child.getErrorCode())) {
                            childErrorCodeList.add(child.getErrorCode());
                        }
                    }
                    if (CollectionUtils.isNotEmpty(childErrorCodeList)) {
                        dto.setErrorCode(StringUtils.join(childErrorCodeList, ","));
                    }
                }

                int failCount = 0;
                int errorReportCount = 0;
                int successCount = 0;
                for (StepTreeDTO child : dto.getChildren()) {
                    if (StringUtils.equalsIgnoreCase(child.getTotalStatus(), ExecuteResult.FAIL.getValue())) {
                        failCount++;
                    } else if (StringUtils.equalsIgnoreCase(child.getTotalStatus(), ExecuteResult.API_SUCCESS.getValue())) {
                        successCount++;
                    } else if (StringUtils.equalsAnyIgnoreCase(child.getTotalStatus(), ERROR_CODE, ERROR_REPORT)) {
                        errorReportCount++;
                    }
                }

                //当有多个子步骤结果时，如果当前步骤不是场景，则：失败>误报>未执行>成功>未执行； 如果是场景：误报>失败>成功>未执行
                if (failCount == 0 && errorReportCount == 0 && successCount == 0) {
                    dto.setTotalStatus(ExecuteResult.UN_EXECUTE.toString());
                } else if (successCount == dto.getChildren().size() || (successCount > 0 && errorReportCount == 0 && failCount == 0)) {
                    dto.setTotalStatus(ExecuteResult.API_SUCCESS.getValue());
                } else {
                    if (StringUtils.equalsIgnoreCase(dto.getType(), SCENARIO)) {
                        if (failCount > 0) {
                            dto.setTotalStatus(ExecuteResult.FAIL.getValue());
                        } else if (errorReportCount > 0) {
                            dto.setTotalStatus(ERROR_CODE);
                        } else {
                            dto.setTotalStatus(ExecuteResult.API_SUCCESS.getValue());
                        }
                    } else {
                        if (failCount > 0) {
                            dto.setTotalStatus(ExecuteResult.FAIL.getValue());
                        } else if (errorReportCount > 0) {
                            dto.setTotalStatus(ERROR_CODE);
                        } else {
                            dto.setTotalStatus(ExecuteResult.API_SUCCESS.getValue());
                        }
                    }
                }
            }
            if (StringUtils.isEmpty(dto.getTotalStatus())) {
                dto.setTotalStatus(ExecuteResult.UN_EXECUTE.toString());
            } else if (StringUtils.equalsAnyIgnoreCase(dto.getTotalStatus(), ERROR)) {
                dto.setTotalStatus(ExecuteResult.FAIL.getValue());
            }
        }
        if (!reportType.startsWith("UI")) {
            this.orderLoops(dtoList);
        }
    }

    /**
     * ui 的报告内容不进行懒加载，统一从 content 中取，由于 v2.1 中合并图片存在 baseInfo
     * 这里做一个兼容处理
     *
     * @param reportResults
     * @param i
     * @param result
     * @return
     */
    private RequestResult getUIRequestResult(List<ApiScenarioReportResultWithBLOBs> reportResults, int i, RequestResult result) {
        String combinationImg = ((RequestResultExpandDTO) result).getCombinationImg();
        result = JSON.parseObject(new String(reportResults.get(i).getContent(), StandardCharsets.UTF_8), UiCommandResult.class);
        ((UiCommandResult) result).calTime();
        if (StringUtils.isNotBlank(combinationImg)) {
            ((UiCommandResult) result).setCombinationImg(combinationImg);
        }
        return result;
    }

    /**
     * 循环步骤请求从新排序
     */
    private void orderLoops(List<StepTreeDTO> dtoList) {
        try {
            List<StepTreeDTO> steps = dtoList.stream().filter(e -> e.getValue() == null || e.getValue().getStartTime() == 0)
                    .collect(Collectors.toList());
            // 都是没有结果的步骤，不需要再次排序
            if (dtoList.size() == steps.size()) {
                return;
            }
            // 非正常执行结束的请求结果
            List<StepTreeDTO> unList = dtoList.stream().filter(e -> e.getValue() != null
                    && ((StringUtils.equalsIgnoreCase(e.getType(), "DubboSampler") && e.getValue().getStartTime() == 0)
                    || StringUtils.equalsIgnoreCase(e.getTotalStatus(), ExecuteResult.UN_EXECUTE.toString())))
                    .collect(Collectors.toList());

            // 有效数据按照时间排序
            List<StepTreeDTO> list = dtoList.stream().filter(e -> e.getValue() != null && e.getValue().getStartTime() != 0).collect(Collectors.toList());
            list = list.stream().sorted(Comparator.comparing(x -> x.getValue().getStartTime())).collect(Collectors.toList());
            unList = unList.stream().sorted(Comparator.comparing(x -> x.getIndex())).collect(Collectors.toList());
            unList.addAll(steps);
            List<StepTreeDTO> mergeList = unList.stream().distinct().collect(Collectors.toList());
            // 处理请求结果开始时间为0的数据
            for (StepTreeDTO unListDTO : mergeList) {
                int index = unListDTO.getIndex();
                if (index > 0) {
                    list.add(index - 1, unListDTO);
                }
            }
            for (int index = 0; index < list.size(); index++) {
                list.get(index).setIndex((index + 1));
            }
            dtoList.clear();
            dtoList.addAll(list);
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private boolean isUiUnExecuteCommand(StepTreeDTO dto) {
        if (dto.getType().equals(UI_COMMAND) && dto.getValue() == null
                && (StringUtils.isBlank(dto.getCmdType()) || !dto.getCmdType().equalsIgnoreCase(CommandType.COMMAND_TYPE_COMBINATION))) {
            return true;
        }
        return false;
    }

    private List<ApiDefinitionExecResultVo> formatApiReport(String reportId, List<StepTreeDTO> stepList) {
        ApiDefinitionExecResultExample example = new ApiDefinitionExecResultExample();
        example.createCriteria().andIntegratedReportIdEqualTo(reportId);
        example.setOrderByClause("create_time asc");
        List<ApiDefinitionExecResultWithBLOBs> reportResults = definitionExecResultMapper.selectByExampleWithBLOBs(example);
        List<ApiDefinitionExecResultVo> resultVos = new LinkedList<>();
        for (int i = 0; i < reportResults.size(); i++) {
            ApiDefinitionExecResultWithBLOBs item = reportResults.get(i);
            if (StringUtils.equalsIgnoreCase(item.getErrorCode(), "null")) {
                item.setErrorCode(null);
            }
            ApiDefinitionExecResultVo vo = new ApiDefinitionExecResultVo();
            BeanUtils.copyBean(vo, item);
            if (StringUtils.isNotEmpty(item.getContent())) {
                try {
                    RequestResultExpandDTO resultExpandDTO = JSON.parseObject(item.getContent(), RequestResultExpandDTO.class);
                    vo.setRequestResult(resultExpandDTO);
                } catch (Exception e) {
                    vo.setRequestResult(JSON.parseObject(item.getContent(), RequestResult.class));
                }
                if (vo.getRequestResult() != null) {
                    vo.setPassAssertions(vo.getRequestResult().getPassAssertions());
                    vo.setTotalAssertions(vo.getRequestResult().getTotalAssertions());
                    vo.getRequestResult().setName(item.getName());
                }
            }
            if (vo.getRequestResult() == null) {
                RequestResultExpandDTO requestResultExpandDTO = new RequestResultExpandDTO();
                requestResultExpandDTO.setStatus(ExecuteResult.UN_EXECUTE.toString());
                requestResultExpandDTO.setName(item.getName());
                vo.setRequestResult(requestResultExpandDTO);
            }
            StepTreeDTO treeDTO = new StepTreeDTO(item.getName(), item.getResourceId(), "API", item.getId(), (i + 1));
            treeDTO.setValue(vo.getRequestResult());
            if (vo.getRequestResult() != null && vo.getRequestResult() instanceof RequestResultExpandDTO) {
                RequestResultExpandDTO expandDTO = (RequestResultExpandDTO) vo.getRequestResult();
                if (expandDTO.getAttachInfoMap() != null && expandDTO.getAttachInfoMap().get(ERROR_REPORT) != null) {
                    treeDTO.setErrorCode(expandDTO.getAttachInfoMap().get(ERROR_REPORT));
                    treeDTO.setTotalStatus(vo.getStatus());
                } else if (StringUtils.isNotEmpty(expandDTO.getStatus())) {
                    vo.setStatus(expandDTO.getStatus());
                    treeDTO.setTotalStatus(expandDTO.getStatus());
                } else {
                    if (expandDTO.isSuccess()) {
                        treeDTO.setTotalStatus(ExecuteResult.API_SUCCESS.getValue());
                    } else {
                        treeDTO.setTotalStatus(ExecuteResult.FAIL.getValue());
                    }
                }
            }

            stepList.add(treeDTO);
            resultVos.add(vo);
        }
        return resultVos;
    }

    public ApiScenarioReportDTO apiIntegratedReport(String reportId) {
        List<StepTreeDTO> stepList = new LinkedList<>();
        List<ApiDefinitionExecResultVo> reportResults = this.formatApiReport(reportId, stepList);
        ApiScenarioReportDTO reportDTO = new ApiScenarioReportDTO();
        // 组装报告
        if (CollectionUtils.isNotEmpty(reportResults)) {
            reportDTO.setTotal(reportResults.size());
            reportDTO.setError(reportResults.stream().filter(e -> StringUtils.equalsAnyIgnoreCase(e.getStatus(), ERROR)).collect(Collectors.toList()).size());
            reportDTO.setUnExecute(reportResults.stream().filter(e -> StringUtils.equalsAnyIgnoreCase(e.getStatus(), "STOP", ExecuteResult.UN_EXECUTE.toString())).collect(Collectors.toList()).size());
            reportDTO.setErrorCode(reportResults.stream().filter(e -> StringUtils.equalsAnyIgnoreCase(e.getStatus(), ERROR_REPORT)).collect(Collectors.toList()).size());
            reportDTO.setPassAssertions(reportResults.stream().mapToLong(ApiDefinitionExecResultVo::getPassAssertions).sum());
            reportDTO.setTotalAssertions(reportResults.stream().mapToLong(ApiDefinitionExecResultVo::getTotalAssertions).sum());

            reportDTO = this.countReportNum(stepList, reportDTO);
            long successStep = reportResults.size() - reportDTO.getScenarioError() - reportDTO.getScenarioErrorReport() - reportDTO.getScenarioUnExecute();
            reportDTO.setScenarioStepSuccess(successStep > 0 ? successStep : 0);
            //统计步骤数据
            reportDTO.setScenarioStepTotal(reportResults.size());
            reportDTO.setScenarioStepError(reportDTO.getError());
            reportDTO.setScenarioStepUnExecuteReport(reportDTO.getUnExecute());
            reportDTO.setScenarioStepErrorReport(reportDTO.getScenarioErrorReport());

            ApiScenarioReportStructureExample structureExample = new ApiScenarioReportStructureExample();
            structureExample.createCriteria().andReportIdEqualTo(reportId);
            List<ApiScenarioReportStructureWithBLOBs> reportStructureWithBLOBs = mapper.selectByExampleWithBLOBs(structureExample);
            if (CollectionUtils.isNotEmpty(reportStructureWithBLOBs)) {
                reportDTO.setConsole(reportStructureWithBLOBs.get(0).getConsole());
            }
            reportDTO.setSteps(stepList);
        }

        return reportDTO;
    }

    public ApiScenarioReportDTO assembleReport(String reportId, boolean selectReportContent) {
        ApiScenarioReportWithBLOBs report = scenarioReportMapper.selectByPrimaryKey(reportId);
        if (report != null && report.getReportType().equals(ReportTypeConstants.API_INTEGRATED.name())) {
            return this.apiIntegratedReport(reportId);
        } else {
            ApiScenarioReportDTO dto = this.getReport(reportId, selectReportContent);
            dto.setActuator(report.getActuator());
            dto.setName(report.getName());
            dto.setEnvConfig(report.getEnvConfig());
            this.initProjectEnvironmentByEnvConfig(dto, report.getEnvConfig());
            return dto;
        }
    }

    public void initProjectEnvironmentByEnvConfig(ApiScenarioReportDTO dto, String envConfig) {
        if (StringUtils.isNotEmpty(envConfig)) {
            LinkedHashMap<String, List<String>> projectEnvMap = apiScenarioEnvService.getProjectEnvMapByEnvConfig(envConfig);
            if (MapUtils.isNotEmpty(projectEnvMap)) {
                dto.setProjectEnvMap(projectEnvMap);
            }
        }
    }


    private ApiScenarioReportDTO getReport(String reportId, boolean selectContent) {
        ApiScenarioReport mainReport = apiScenarioReportMapper.selectByPrimaryKey(reportId);
        List<ApiScenarioReportResultWithBLOBs> reportResults = null;
        if (selectContent || mainReport.getReportType().startsWith(SystemConstants.TestTypeEnum.UI.name())) {
            // UI 报告不执行懒加载
            ApiScenarioReportResultExample example = new ApiScenarioReportResultExample();
            example.createCriteria().andReportIdEqualTo(reportId);
            reportResults = reportResultMapper.selectByExampleWithBLOBs(example);
        } else {
            reportResults = this.selectBaseInfoResultByReportId(reportId);
            //判断base_info是否为空，为空则是旧数据
            boolean isBaseInfoNull = false;
            for (ApiScenarioReportResultWithBLOBs result : reportResults) {
                if (result.getBaseInfo() == null) {
                    isBaseInfoNull = true;
                    break;
                }
            }
            if (isBaseInfoNull) {
                ApiScenarioReportResultExample example = new ApiScenarioReportResultExample();
                example.createCriteria().andReportIdEqualTo(reportId);
                reportResults = reportResultMapper.selectByExampleWithBLOBs(example);
            }
        }

        removeUiResultIfNotStep(reportResults, reportId);

        ApiScenarioReportStructureExample structureExample = new ApiScenarioReportStructureExample();
        structureExample.createCriteria().andReportIdEqualTo(reportId);
        List<ApiScenarioReportStructureWithBLOBs> reportStructureWithBLOBs = mapper.selectByExampleWithBLOBs(structureExample);

        ApiScenarioReportDTO reportDTO = new ApiScenarioReportDTO();
        // 写入控制台信息
        if (CollectionUtils.isNotEmpty(reportStructureWithBLOBs)) {
            reportDTO.setConsole(reportStructureWithBLOBs.get(0).getConsole());
        }
        // 组装报告
        if (CollectionUtils.isNotEmpty(reportStructureWithBLOBs) && CollectionUtils.isNotEmpty(reportResults)) {
            ApiScenarioReportStructureWithBLOBs scenarioReportStructure = reportStructureWithBLOBs.get(0);
            List<StepTreeDTO> stepList = JSONArray.parseArray(new String(scenarioReportStructure.getResourceTree(), StandardCharsets.UTF_8), StepTreeDTO.class);
            //判断是否含有全局前后置脚本，如果有的话需要将脚本内容添加到stepDTO中
            reportResults = this.filterProcessResult(reportResults);

            reportDTO.setTotal(reportResults.size());
            reportDTO.setError(reportResults.stream().filter(e -> StringUtils.equals(e.getStatus(), ERROR)).collect(Collectors.toList()).size());
            reportDTO.setErrorCode(reportResults.stream().filter(e -> StringUtils.equals(e.getStatus(), ExecuteResult.ERROR_REPORT_RESULT.toString())).collect(Collectors.toList()).size());
            reportDTO.setPassAssertions(reportResults.stream().mapToLong(ApiScenarioReportResult::getPassAssertions).sum());
            reportDTO.setTotalAssertions(reportResults.stream().mapToLong(ApiScenarioReportResult::getTotalAssertions).sum());


            // 匹配结果
            Map<String, List<ApiScenarioReportResultWithBLOBs>> maps = reportResults.stream().collect(Collectors.groupingBy(ApiScenarioReportResult::getResourceId));
            this.reportFormatting(stepList, maps, mainReport.getReportType());

            reportDTO = this.countReportNum(stepList, reportDTO);
            // 统计场景数据
            AtomicLong stepError = new AtomicLong();
            AtomicLong stepTotal = new AtomicLong();

            reportDTO.setScenarioSuccess((reportDTO.getScenarioTotal() - reportDTO.getScenarioError() - reportDTO.getScenarioUnExecute() - reportDTO.getScenarioErrorReport()));

            //统计步骤数据
            AtomicLong stepErrorCode = new AtomicLong();
            AtomicLong stepUnExecute = new AtomicLong();
            calculateStep(stepList, stepTotal, stepError, stepErrorCode, stepUnExecute);

            reportDTO.setScenarioStepSuccess((stepTotal.longValue() - stepError.longValue() - stepErrorCode.longValue() - stepUnExecute.longValue()));
            reportDTO.setScenarioStepTotal(stepTotal.longValue());
            reportDTO.setScenarioStepError(stepError.longValue());
            reportDTO.setScenarioStepErrorReport(stepErrorCode.longValue());
            reportDTO.setScenarioStepUnExecuteReport(stepUnExecute.longValue());
            reportDTO.setConsole(scenarioReportStructure.getConsole());
            reportDTO.setSteps(stepList);

            //统计未执行的请求数量
            AtomicLong allUnExecute = new AtomicLong();
            this.countAllUnexpected(stepList, allUnExecute);
            reportDTO.setUnExecute(allUnExecute.longValue());
            //之前的total中请求数是按照获得报告的响应数来算的。这里要加上未执行的数量
            reportDTO.setTotal(reportDTO.getTotal() + allUnExecute.longValue());
        }
        return reportDTO;
    }

    private List<ApiScenarioReportResultWithBLOBs> filterProcessResult(List<ApiScenarioReportResultWithBLOBs> reportResults) {
        List<ApiScenarioReportResultWithBLOBs> withOutProcessList = new ArrayList<>();
        for (ApiScenarioReportResultWithBLOBs item : reportResults) {
            if (item.getBaseInfo() != null) {
                ApiScenarioReportBaseInfoDTO dto = JSONObject.parseObject(item.getBaseInfo(), ApiScenarioReportBaseInfoDTO.class);
                if (!StringUtils.startsWithAny(dto.getReqName(), ResultParseUtil.PRE_PROCESS_SCRIPT, ResultParseUtil.POST_PROCESS_SCRIPT)) {
                    withOutProcessList.add(item);
                }
            } else {
                withOutProcessList.add(item);
            }
        }
        return withOutProcessList;
    }

    private List<ApiScenarioReportResultWithBLOBs> selectBaseInfoResultByReportId(String reportId) {
        return extApiScenarioReportResultMapper.selectBaseInfoResultByReportId(reportId);
    }

    /**
     * UI 测试结果统计去掉前后置或其他不算步骤的执行结果
     *
     * @param reportResults
     */
    private void removeUiResultIfNotStep(List<ApiScenarioReportResultWithBLOBs> reportResults, String reportId) {
        ApiScenarioReport report = scenarioReportMapper.selectByPrimaryKey(reportId);
        if (report.getReportType() != null && report.getReportType().startsWith("UI")) {
            if (CollectionUtils.isNotEmpty(reportResults)) {
                Iterator<ApiScenarioReportResultWithBLOBs> iterator = reportResults.iterator();
                while (iterator.hasNext()) {
                    ApiScenarioReportResultWithBLOBs item = iterator.next();
                    String baseInfo = item.getBaseInfo();
                    if (StringUtils.isNotBlank(baseInfo)) {
                        Boolean isNoStep = JSONObject.parseObject(baseInfo).getBoolean("isNotStep");
                        if (BooleanUtils.isTrue(isNoStep)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    private void countAllUnexpected(List<StepTreeDTO> stepList, AtomicLong allUnExecute) {
        for (StepTreeDTO step : stepList) {
            if (step.getValue() != null) {
                if (step.getValue() instanceof RequestResultExpandDTO
                        && StringUtils.equalsIgnoreCase(((RequestResultExpandDTO) step.getValue()).getStatus(), ExecuteResult.UN_EXECUTE.toString())) {
                    allUnExecute.set(allUnExecute.longValue() + 1);
                }
            }
            if (CollectionUtils.isNotEmpty(step.getChildren())) {
                this.countAllUnexpected(step.getChildren(), allUnExecute);
            }
        }
    }

    private ApiScenarioReportDTO countReportNum(List<StepTreeDTO> stepList, ApiScenarioReportDTO reportDTO) {
        if (reportDTO != null && CollectionUtils.isNotEmpty(stepList)) {
            // 统计场景数据
            AtomicLong totalScenario = new AtomicLong();
            AtomicLong scenarioError = new AtomicLong();
            AtomicLong totalTime = new AtomicLong();
            AtomicLong errorReport = new AtomicLong();
            AtomicLong unExecute = new AtomicLong();
            calculateScenarios(stepList, totalScenario, scenarioError, errorReport, unExecute);
            calculate(stepList, totalTime);
            reportDTO.setTotalTime(totalTime.longValue());
            reportDTO.setScenarioTotal(totalScenario.longValue());
            reportDTO.setScenarioError(scenarioError.longValue());
            reportDTO.setScenarioErrorReport(errorReport.longValue());
            reportDTO.setScenarioUnExecute(unExecute.longValue());
        }
        return reportDTO;
    }

    public RequestResult selectReportContent(String stepId) {
        RequestResult result = new RequestResult();
        try {
            result = selectReportContent(stepId, RequestResult.class);
        } catch (Exception ignore) {
        }
        return result;
    }

    public <T> T selectReportContent(String stepId, Class clazz) {
        ApiScenarioReportResultWithBLOBs apiScenarioReportResult = reportResultMapper.selectByPrimaryKey(stepId);
        if (apiScenarioReportResult != null) {
            T requestResult = JSON.parseObject(new String(apiScenarioReportResult.getContent(), StandardCharsets.UTF_8), (Type) clazz);
            return requestResult;
        } else {
            return (T) clazz.getInterfaces();
        }
    }
}
