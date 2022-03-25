package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiScenarioReportDTO;
import io.metersphere.api.dto.RequestResultExpandDTO;
import io.metersphere.api.dto.StepTreeDTO;
import io.metersphere.api.service.vo.ApiDefinitionExecResultVo;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.*;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.constants.ReportTypeConstants;
import io.metersphere.commons.utils.BeanUtils;
import io.metersphere.commons.utils.CommonBeanFactory;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.constants.RunModeConstants;
import io.metersphere.dto.RequestResult;
import io.metersphere.utils.LoggerUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class ApiScenarioReportStructureService {

    @Resource
    private ApiScenarioReportStructureMapper mapper;
    @Resource
    private ApiScenarioReportResultMapper reportResultMapper;
    @Resource
    private ApiScenarioReportMapper scenarioReportMapper;
    @Resource
    private ApiDefinitionExecResultMapper definitionExecResultMapper;

    private static final List<String> requests = Arrays.asList("HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler", "JSR223Processor", "AbstractSampler");
    private static final List<String> controls = Arrays.asList("Assertions","IfController","ConstantTimer");

    public void save(List<ApiScenarioWithBLOBs> apiScenarios, String reportId, String reportType) {
        List<StepTreeDTO> dtoList = new LinkedList<>();
        for (ApiScenarioWithBLOBs bos : apiScenarios) {
            StepTreeDTO dto = dataFormatting(bos, reportType);
            dtoList.add(dto);
        }
        if (LoggerUtil.getLogger().isDebugEnabled()) {
            LoggerUtil.debug("Scenario run-执行脚本装载-生成场景报告结构：" + JSON.toJSONString(dtoList));
        }
        this.save(reportId, dtoList);
    }

    public void save(ApiScenarioWithBLOBs apiScenario, String reportId, String reportType) {
        List<StepTreeDTO> dtoList = new LinkedList<>();
        StepTreeDTO dto = dataFormatting(apiScenario, reportType);
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
        JSONObject element = JSON.parseObject(apiScenario.getScenarioDefinition());
        StepTreeDTO dto = null;
        if (element != null && element.getBoolean("enable")) {
            element = getRefElement(element);
            String resourceId = StringUtils.isNotEmpty(element.getString("id"))
                    ? element.getString("id") : element.getString("resourceId");

            if (StringUtils.equals(reportType, RunModeConstants.SET_REPORT.toString())) {
                if (StringUtils.isNotEmpty(resourceId) && StringUtils.isNotEmpty(apiScenario.getId()) && !resourceId.contains(apiScenario.getId())) {
                    resourceId = apiScenario.getId() + "=" + resourceId;
                }
            }
            dto = new StepTreeDTO(apiScenario.getName(), resourceId, element.getString("type"), 1);
            dto.setAllIndex(null);
            if (element.containsKey("hashTree") && !requests.contains(dto.getType())) {
                JSONArray elementJSONArray = element.getJSONArray("hashTree");
                dataFormatting(elementJSONArray, dto, apiScenario.getId(), reportType);
            }
        }
        return dto;
    }

    private static JSONObject getRefElement(JSONObject element) {
        String referenced = element.getString("referenced");
        if (StringUtils.equals(referenced, MsTestElementConstants.REF.name())) {
            if (StringUtils.equals(element.getString("type"), "scenario")) {
                ApiScenarioWithBLOBs scenarioWithBLOBs = CommonBeanFactory.getBean(ApiScenarioMapper.class).selectByPrimaryKey(element.getString("id"));
                if (scenarioWithBLOBs != null) {
                    return JSON.parseObject(scenarioWithBLOBs.getScenarioDefinition());
                }
            }
        }
        return element;
    }

    public static void dataFormatting(JSONArray hashTree, StepTreeDTO dto, String id, String reportType) {
        for (int i = 0; i < hashTree.size(); i++) {
            JSONObject element = hashTree.getJSONObject(i);
            if (element != null && element.getBoolean("enable")) {
                element = getRefElement(element);
                String resourceId = StringUtils.isNotEmpty(element.getString("id"))
                        ? element.getString("id") : element.getString("resourceId");
                if (StringUtils.equals(reportType, RunModeConstants.SET_REPORT.toString())) {
                    if (StringUtils.isNotEmpty(resourceId) && StringUtils.isNotEmpty(id) && !resourceId.contains(id)) {
                        resourceId = id + "=" + resourceId;
                    }
                }
                StepTreeDTO children = new StepTreeDTO(element.getString("name"), resourceId, element.getString("type"), element.getIntValue("index"));
                if (StringUtils.isNotEmpty(dto.getAllIndex())) {
                    children.setAllIndex(dto.getAllIndex() + "_" + (children.getIndex() == 0 ? (i + 1) : children.getIndex()));
                    children.setResourceId(resourceId + "_" + children.getAllIndex());
                } else {
                    children.setAllIndex("" + (children.getIndex() == 0 ? (i + 1) : children.getIndex()));
                    children.setResourceId(resourceId + "_" + children.getAllIndex());
                }
                dto.getChildren().add(children);
                if (element.containsKey("hashTree") && !requests.contains(children.getType())) {
                    JSONArray elementJSONArray = element.getJSONArray("hashTree");
                    dataFormatting(elementJSONArray, children, id, reportType);
                }
            }
        }
    }

    private void scenarioCalculate(List<StepTreeDTO> dtoList, AtomicLong isError, AtomicLong isErrorReport, AtomicLong isUnExecute, boolean errorIsFirst) {
        /**
         * 判断场景步骤的执行状态
         * 失败状态的优先级最高，其次是误报
         */
        for (StepTreeDTO step : dtoList) {
            if (step.getValue() != null) {
                if (step.getValue() instanceof RequestResultExpandDTO
                        && StringUtils.equalsIgnoreCase(((RequestResultExpandDTO) step.getValue()).getStatus(), "unexecute")) {
                    isUnExecute.set(isUnExecute.longValue() + 1);
                } else if (StringUtils.isNotEmpty(step.getErrorCode())) {
                    isErrorReport.set(isErrorReport.longValue() + 1);
                } else if (step.getValue().getError() > 0 || !step.getValue().isSuccess()) {
                    isError.set(isError.longValue() + 1);
                }
            } else if (CollectionUtils.isNotEmpty(step.getChildren())) {
                AtomicLong isChildrenError = new AtomicLong();
                AtomicLong isChildrenErrorReport = new AtomicLong();
                AtomicLong isChildrenUnExecute = new AtomicLong();
                stepChildrenErrorCalculate(step.getChildren(), isChildrenError, isChildrenErrorReport, isChildrenUnExecute);
                if (isChildrenUnExecute.longValue() > 0) {
                    isUnExecute.set(isUnExecute.longValue() + 1);
                } else if (errorIsFirst) {
                    if (isChildrenError.longValue() > 0) {
                        isError.set(isError.longValue() + 1);
                    } else if (isChildrenErrorReport.longValue() > 0) {
                        isErrorReport.set(isErrorReport.longValue() + 1);
                    }
                } else {
                    if (isChildrenErrorReport.longValue() > 0) {
                        isErrorReport.set(isErrorReport.longValue() + 1);
                    } else if (isChildrenError.longValue() > 0) {
                        isError.set(isError.longValue() + 1);
                    }
                }

            }
        }
    }

    private void calculateScenarios(List<StepTreeDTO> dtoList,
                                    AtomicLong totalScenario, AtomicLong scenarioError, AtomicLong errorReport, AtomicLong unExecute) {
        for (StepTreeDTO step : dtoList) {
            totalScenario.set(totalScenario.longValue() + 1);
            if (StringUtils.equalsIgnoreCase(step.getTotalStatus(), "fail")) {
                scenarioError.set(scenarioError.longValue() + 1);
            } else if (StringUtils.equalsIgnoreCase(step.getTotalStatus(), "errorCode")) {
                errorReport.set(errorReport.longValue() + 1);
            } else if (!StringUtils.equalsIgnoreCase(step.getTotalStatus(), "success")) {
                unExecute.set(unExecute.longValue() + 1);
            }
        }
    }

    private void calculate(List<StepTreeDTO> dtoList, AtomicLong totalTime) {
        for (StepTreeDTO step : dtoList) {
            if (!StringUtils.equals(step.getType(), "scenario") && step.getValue() != null) {
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
            if (CollectionUtils.isNotEmpty(root.getChildren())) {
                stepTotal.set((stepTotal.longValue() + root.getChildren().size()));
                for (StepTreeDTO step : root.getChildren()) {
                    if (StringUtils.equalsIgnoreCase(step.getTotalStatus(), "fail")) {
                        stepError.set(stepError.longValue() + 1);
                    } else if (StringUtils.equalsIgnoreCase(step.getTotalStatus(), "errorCode")) {
                        stepErrorCode.set(stepErrorCode.longValue() + 1);
                    } else if (!StringUtils.equalsIgnoreCase(step.getTotalStatus(), "success")) {
                        stepUnExecute.set(stepUnExecute.longValue() + 1);
                    }
                }
            }
        }
    }

    public static void reportFormatting(List<StepTreeDTO> dtoList, Map<String, List<ApiScenarioReportResult>> maps) {
        for (int index = 0; index < dtoList.size(); index++) {
            StepTreeDTO dto = dtoList.get(index);
            dto.setIndex((index + 1));
            List<ApiScenarioReportResult> reportResults = maps.get(dto.getResourceId());
            if (CollectionUtils.isNotEmpty(reportResults)) {
                if (reportResults.size() > 1) {
                    for (int i = 0; i < reportResults.size(); i++) {
                        if (i == 0) {
                            dto.setValue(JSON.parseObject(new String(reportResults.get(i).getContent(), StandardCharsets.UTF_8), RequestResult.class));
                            dto.setErrorCode(reportResults.get(0).getErrorCode());
                        } else {
                            StepTreeDTO step = new StepTreeDTO(dto.getLabel(), UUID.randomUUID().toString(), dto.getType(), (i + 1));
                            step.setValue(JSON.parseObject(new String(reportResults.get(i).getContent(), StandardCharsets.UTF_8), RequestResult.class));
                            step.setErrorCode(reportResults.get(i).getErrorCode());
                            dtoList.add(step);
                        }
                    }
                } else {
                    String content = new String(reportResults.get(0).getContent(), StandardCharsets.UTF_8);
                    dto.setValue(JSON.parseObject(content, RequestResult.class));
                    dto.setErrorCode(reportResults.get(0).getErrorCode());
                }
            }
            if (StringUtils.isNotEmpty(dto.getType()) && requests.contains(dto.getType()) && dto.getValue() == null) {
                RequestResultExpandDTO requestResultExpandDTO = new RequestResultExpandDTO();
                requestResultExpandDTO.setStatus("unexecute");
                requestResultExpandDTO.setName(dto.getLabel());
                dto.setTotalStatus("unexecute");
                dto.setValue(requestResultExpandDTO);
            } else if(StringUtils.isNotEmpty(dto.getType()) && controls.contains(dto.getType()) && dto.getValue() == null){
                RequestResultExpandDTO requestResultExpandDTO = new RequestResultExpandDTO();
                requestResultExpandDTO.setStatus("success");
                requestResultExpandDTO.setName(dto.getLabel());
                dto.setTotalStatus("success");
                dto.setValue(requestResultExpandDTO);
            } else {
                if (dto.getValue() instanceof RequestResultExpandDTO && StringUtils.isNotEmpty(((RequestResultExpandDTO) dto.getValue()).getStatus())) {
                    dto.setTotalStatus(((RequestResultExpandDTO) dto.getValue()).getStatus());
                } else if (dto.getValue() != null) {
                    if (dto.getValue().getError() > 0) {
                        dto.setTotalStatus("fail");
                    } else {
                        dto.setTotalStatus("success");
                    }
                }
            }
            if (StringUtils.isNotEmpty(dto.getErrorCode())) {
                dto.setTotalStatus("errorCode");
            }

            if (CollectionUtils.isNotEmpty(dto.getChildren())) {
                reportFormatting(dto.getChildren(), maps);

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
                    if (StringUtils.equalsIgnoreCase(child.getTotalStatus(), "fail")) {
                        failCount++;
                    } else if (StringUtils.equalsIgnoreCase(child.getTotalStatus(), "success")) {
                        successCount++;
                    } else if (StringUtils.equalsIgnoreCase(child.getTotalStatus(), "errorCode")) {
                        errorReportCount++;
                    }
                }

                //当有多个子步骤结果时，如果当前步骤不是场景，则：失败>误报>未执行>成功>未执行； 如果是场景：误报>失败>成功>未执行
                if (failCount == 0 && errorReportCount == 0 && successCount == 0) {
                    dto.setTotalStatus("unexecute");
                } else if (successCount == dto.getChildren().size() || (successCount > 0 && errorReportCount == 0 && failCount == 0)) {
                    dto.setTotalStatus("success");
                } else {
                    if (StringUtils.equalsIgnoreCase(dto.getType(), "scenario")) {
                        if (failCount > 0) {
                            dto.setTotalStatus("fail");
                        } else if (errorReportCount > 0) {
                            dto.setTotalStatus("errorCode");
                        } else {
                            dto.setTotalStatus("success");
                        }
                    } else {
                        if (failCount > 0) {
                            dto.setTotalStatus("fail");
                        } else if (errorReportCount > 0) {
                            dto.setTotalStatus("errorCode");
                        } else {
                            dto.setTotalStatus("success");
                        }
                    }
                }

            }
            if (StringUtils.isEmpty(dto.getTotalStatus())) {
                dto.setTotalStatus("unexecute");
            }
        }
        // 循环步骤请求从新排序
        try {
            if (dtoList.stream().filter(e -> e.getValue() != null).collect(Collectors.toList()).size() == dtoList.size()) {
                List<StepTreeDTO> unList = dtoList.stream().filter(e -> e.getValue() != null
                        && StringUtils.equalsIgnoreCase(e.getTotalStatus(), "unexecute")).collect(Collectors.toList());
                Map<String, Integer> map = unList.stream().collect(Collectors.toMap(StepTreeDTO::getResourceId, StepTreeDTO::getIndex));
                List<StepTreeDTO> list = dtoList.stream().sorted(Comparator.comparing(x -> x.getValue().getStartTime())).collect(Collectors.toList());
                for (int index = 0; index < list.size(); index++) {
                    if (map.containsKey(list.get(index).getResourceId())) {
                        Collections.swap(list, index, (map.get(list.get(index).getResourceId()) - 1));
                    }
                }
                for (int index = 0; index < list.size(); index++) {
                    list.get(index).setIndex((index + 1));
                }
                dtoList.clear();
                dtoList.addAll(list);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
    }

    private List<ApiDefinitionExecResultVo> formatApiReport(String reportId, List<StepTreeDTO> stepList) {
        ApiDefinitionExecResultExample example = new ApiDefinitionExecResultExample();
        example.createCriteria().andIntegratedReportIdEqualTo(reportId);
        example.setOrderByClause("create_time asc");
        List<ApiDefinitionExecResult> reportResults = definitionExecResultMapper.selectByExampleWithBLOBs(example);
        List<ApiDefinitionExecResultVo> resultVos = new LinkedList<>();
        for (int i = 0; i < reportResults.size(); i++) {
            ApiDefinitionExecResult item = reportResults.get(i);
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
                requestResultExpandDTO.setStatus("unexecute");
                requestResultExpandDTO.setName(item.getName());
                vo.setRequestResult(requestResultExpandDTO);
            }
            StepTreeDTO treeDTO = new StepTreeDTO(item.getName(), item.getResourceId(), "API", (i + 1));
            treeDTO.setValue(vo.getRequestResult());
            if (vo.getRequestResult() != null && vo.getRequestResult() instanceof RequestResultExpandDTO) {
                RequestResultExpandDTO expandDTO = (RequestResultExpandDTO) vo.getRequestResult();
                if (expandDTO.getAttachInfoMap() != null && expandDTO.getAttachInfoMap().get("errorReportResult") != null) {
                    treeDTO.setErrorCode(expandDTO.getAttachInfoMap().get("errorReportResult"));
                    treeDTO.setTotalStatus("errorCode");
                    vo.setStatus("errorReportResult");
                } else if (StringUtils.isNotEmpty(expandDTO.getStatus())) {
                    vo.setStatus(expandDTO.getStatus());
                    treeDTO.setTotalStatus(expandDTO.getStatus());
                } else {
                    if (expandDTO.isSuccess()) {
                        treeDTO.setTotalStatus("success");
                    } else {
                        treeDTO.setTotalStatus("fail");
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
            reportDTO.setError(reportResults.stream().filter(e -> StringUtils.equalsAnyIgnoreCase(e.getStatus(), "Error")).collect(Collectors.toList()).size());
            reportDTO.setUnExecute(reportResults.stream().filter(e -> StringUtils.equalsAnyIgnoreCase(e.getStatus(), "STOP","unexecute")).collect(Collectors.toList()).size());
            reportDTO.setErrorCode(reportResults.stream().filter(e -> StringUtils.equalsAnyIgnoreCase(e.getStatus(), "errorReportResult")).collect(Collectors.toList()).size());
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

    public ApiScenarioReportDTO assembleReport(String reportId) {
        ApiScenarioReport report = scenarioReportMapper.selectByPrimaryKey(reportId);
        if (report != null && report.getReportType().equals(ReportTypeConstants.API_INTEGRATED.name())) {
            return this.apiIntegratedReport(reportId);
        } else {
            return this.getReport(reportId);
        }
    }

    private ApiScenarioReportDTO getReport(String reportId) {
        ApiScenarioReportResultExample example = new ApiScenarioReportResultExample();
        example.createCriteria().andReportIdEqualTo(reportId);
        List<ApiScenarioReportResult> reportResults = reportResultMapper.selectByExampleWithBLOBs(example);

        ApiScenarioReportStructureExample structureExample = new ApiScenarioReportStructureExample();
        structureExample.createCriteria().andReportIdEqualTo(reportId);
        List<ApiScenarioReportStructureWithBLOBs> reportStructureWithBLOBs = mapper.selectByExampleWithBLOBs(structureExample);

        ApiScenarioReportDTO reportDTO = new ApiScenarioReportDTO();
        // 组装报告
        if (CollectionUtils.isNotEmpty(reportStructureWithBLOBs) && CollectionUtils.isNotEmpty(reportResults)) {
            reportDTO.setTotal(reportResults.size());
            reportDTO.setError(reportResults.stream().filter(e -> StringUtils.equals(e.getStatus(), "Error")).collect(Collectors.toList()).size());
            reportDTO.setErrorCode(reportResults.stream().filter(e -> StringUtils.isNotEmpty(e.getErrorCode())).collect(Collectors.toList()).size());
            reportDTO.setPassAssertions(reportResults.stream().mapToLong(ApiScenarioReportResult::getPassAssertions).sum());
            reportDTO.setTotalAssertions(reportResults.stream().mapToLong(ApiScenarioReportResult::getTotalAssertions).sum());
            ApiScenarioReportStructureWithBLOBs scenarioReportStructure = reportStructureWithBLOBs.get(0);
            List<StepTreeDTO> stepList = JSONArray.parseArray(new String(scenarioReportStructure.getResourceTree(), StandardCharsets.UTF_8), StepTreeDTO.class);
            // 匹配结果
            Map<String, List<ApiScenarioReportResult>> maps = reportResults.stream().collect(Collectors.groupingBy(ApiScenarioReportResult::getResourceId));
            this.reportFormatting(stepList, maps);

            reportDTO = this.countReportNum(stepList, reportDTO);
            // 统计场景数据
            AtomicLong stepError = new AtomicLong();
            AtomicLong stepTotal = new AtomicLong();

            reportDTO.setScenarioSuccess((reportDTO.getScenarioTotal() - reportDTO.getScenarioError() - reportDTO.getScenarioUnExecute() -reportDTO.getScenarioErrorReport()));

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
            this.countAllUnexecute(stepList, allUnExecute);
            reportDTO.setUnExecute(allUnExecute.longValue());
        }
        return reportDTO;
    }

    private void countAllUnexecute(List<StepTreeDTO> stepList, AtomicLong allUnExecute) {
        for (StepTreeDTO step : stepList) {
            if (step.getValue() != null) {
                if (step.getValue() instanceof RequestResultExpandDTO
                        && StringUtils.equalsIgnoreCase(((RequestResultExpandDTO) step.getValue()).getStatus(), "unexecute")) {
                    allUnExecute.set(allUnExecute.longValue() + 1);
                }
            }
            if (CollectionUtils.isNotEmpty(step.getChildren())) {
                this.countAllUnexecute(step.getChildren(), allUnExecute);
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


    private void stepChildrenErrorCalculate(List<StepTreeDTO> dtoList, AtomicLong isError, AtomicLong isErrorReport, AtomicLong isUnExecute) {
        for (StepTreeDTO step : dtoList) {
            if (step.getValue() != null) {
                if (step.getValue() instanceof RequestResultExpandDTO
                        && StringUtils.equalsIgnoreCase(((RequestResultExpandDTO) step.getValue()).getStatus(), "unexecute")) {
                    isUnExecute.set(isUnExecute.longValue() + 1);
                } else if (step.getValue().getError() > 0 || !step.getValue().isSuccess()) {
                    isError.set(isError.longValue() + 1);
                }
                if (StringUtils.isNotEmpty(step.getErrorCode())) {
                    isErrorReport.set(isErrorReport.longValue() + 1);
                }
            } else if (CollectionUtils.isNotEmpty(step.getChildren())) {
                stepChildrenErrorCalculate(step.getChildren(), isError, isErrorReport, isUnExecute);
            }
        }
    }
}
