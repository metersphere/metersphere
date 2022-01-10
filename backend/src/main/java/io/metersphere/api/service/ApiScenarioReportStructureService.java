package io.metersphere.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiScenarioReportDTO;
import io.metersphere.api.dto.StepTreeDTO;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ApiScenarioMapper;
import io.metersphere.base.mapper.ApiScenarioReportResultMapper;
import io.metersphere.base.mapper.ApiScenarioReportStructureMapper;
import io.metersphere.commons.constants.MsTestElementConstants;
import io.metersphere.commons.utils.CommonBeanFactory;
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

    private static final List<String> requests = Arrays.asList("HTTPSamplerProxy", "DubboSampler", "JDBCSampler", "TCPSampler", "JSR223Processor", "AbstractSampler");

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

    private void save(String reportId, List<StepTreeDTO> dtoList) {
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
            String referenced = element.getString("referenced");
            String type = element.getString("type");
            if (StringUtils.equals(referenced, MsTestElementConstants.REF.name())) {
                if (StringUtils.equals(element.getString("type"), "scenario")) {
                    ApiScenarioWithBLOBs scenarioWithBLOBs = CommonBeanFactory.getBean(ApiScenarioMapper.class).selectByPrimaryKey(element.getString("id"));
                    if (scenarioWithBLOBs != null) {
                        element = JSON.parseObject(scenarioWithBLOBs.getScenarioDefinition());
                    }
                }
            }
            String resourceId = "JSR223Processor".equals(type) ? element.getString("resourceId") : element.getString("id");
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

    public static void dataFormatting(JSONArray hashTree, StepTreeDTO dto, String id, String reportType) {
        for (int i = 0; i < hashTree.size(); i++) {
            JSONObject element = hashTree.getJSONObject(i);
            if (element != null && element.getBoolean("enable")) {
                String referenced = element.getString("referenced");
                if (StringUtils.equals(referenced, MsTestElementConstants.REF.name())) {
                    if (StringUtils.equals(element.getString("type"), "scenario")) {
                        ApiScenarioWithBLOBs scenarioWithBLOBs = CommonBeanFactory.getBean(ApiScenarioMapper.class).selectByPrimaryKey(element.getString("id"));
                        if (scenarioWithBLOBs != null) {
                            element = JSON.parseObject(scenarioWithBLOBs.getScenarioDefinition());
                        }
                    }
                }
                String type = element.getString("type");
                String resourceId = "JSR223Processor".equals(type) ? element.getString("resourceId") : element.getString("id");
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

    private void scenarioCalculate(List<StepTreeDTO> dtoList, AtomicLong isError) {
        for (StepTreeDTO step : dtoList) {
            if (step.getValue() != null && step.getValue().getError() > 0) {
                isError.set(isError.longValue() + 1);
                break;
            } else if (CollectionUtils.isNotEmpty(step.getChildren())) {
                scenarioCalculate(step.getChildren(), isError);
            }
        }
    }

    private void stepErrorCalculate(List<StepTreeDTO> dtoList, AtomicLong isError) {
        for (StepTreeDTO step : dtoList) {
            if (step.getValue() != null && step.getValue().getError() > 0) {
                isError.set(isError.longValue() + 1);
            } else if (CollectionUtils.isNotEmpty(step.getChildren())) {
                AtomicLong isChildrenError = new AtomicLong();
                stepChildrenErrorCalculate(step.getChildren(), isChildrenError);
                if (isChildrenError.longValue() > 0) {
                    isError.set(isError.longValue() + 1);
                }
            }
        }
    }

    private void stepChildrenErrorCalculate(List<StepTreeDTO> dtoList, AtomicLong isError) {
        for (StepTreeDTO step : dtoList) {
            if (step.getValue() != null && step.getValue().getError() > 0) {
                isError.set(isError.longValue() + 1);
                break;
            } else if (CollectionUtils.isNotEmpty(step.getChildren())) {
                stepChildrenErrorCalculate(step.getChildren(), isError);
            }
        }
    }

    private void calculate(List<StepTreeDTO> dtoList, AtomicLong totalScenario, AtomicLong scenarioError, AtomicLong totalTime) {
        for (StepTreeDTO step : dtoList) {
            if (StringUtils.equals(step.getType(), "scenario")) {
                totalScenario.set((totalScenario.longValue() + 1));
                // 失败结果数量
                AtomicLong error = new AtomicLong();
                scenarioCalculate(step.getChildren(), error);
                if (error.longValue() > 0) {
                    scenarioError.set((scenarioError.longValue() + 1));
                }
            } else if (step.getValue() != null) {
                if (step.getValue().getStartTime() == 0 || step.getValue().getEndTime() == 0) {
                    totalTime.set(totalTime.longValue() + 0);
                } else {
                    totalTime.set((totalTime.longValue() + (step.getValue().getEndTime() - step.getValue().getStartTime())));
                }
            }
            if (CollectionUtils.isNotEmpty(step.getChildren())) {
                calculate(step.getChildren(), totalScenario, scenarioError, totalTime);
            }
        }
    }

    private void calculateStep(List<StepTreeDTO> dtoList, AtomicLong stepError, AtomicLong stepTotal) {
        for (StepTreeDTO step : dtoList) {
            // 失败结果数量
            stepErrorCalculate(step.getChildren(), stepError);
            if (CollectionUtils.isNotEmpty(step.getChildren())) {
                stepTotal.set((stepTotal.longValue() + step.getChildren().size()));
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
                        } else {
                            StepTreeDTO step = new StepTreeDTO(dto.getLabel(), UUID.randomUUID().toString(), dto.getType(), (i + 1));
                            step.setValue(JSON.parseObject(new String(reportResults.get(i).getContent(), StandardCharsets.UTF_8), RequestResult.class));
                            dtoList.add(step);
                        }
                    }
                } else {
                    String content = new String(reportResults.get(0).getContent(), StandardCharsets.UTF_8);
                    dto.setValue(JSON.parseObject(content, RequestResult.class));
                }
            }
            if (CollectionUtils.isNotEmpty(dto.getChildren())) {
                reportFormatting(dto.getChildren(), maps);
            }
        }
        // 循环步骤请求从新排序
        if (dtoList.stream().filter(e -> e.getValue() != null).collect(Collectors.toList()).size() == dtoList.size()) {
            List<StepTreeDTO> list = dtoList.stream().sorted(Comparator.comparing(x -> x.getValue().getStartTime())).collect(Collectors.toList());
            for (int index = 0; index < list.size(); index++) {
                list.get(index).setIndex((index + 1));
            }
            dtoList.clear();
            dtoList.addAll(list);
        }
    }

    public ApiScenarioReportDTO getReport(String reportId) {
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
            reportDTO.setPassAssertions(reportResults.stream().mapToLong(ApiScenarioReportResult::getPassAssertions).sum());
            reportDTO.setTotalAssertions(reportResults.stream().mapToLong(ApiScenarioReportResult::getTotalAssertions).sum());

            ApiScenarioReportStructureWithBLOBs scenarioReportStructure = reportStructureWithBLOBs.get(0);
            List<StepTreeDTO> stepList = JSONArray.parseArray(new String(scenarioReportStructure.getResourceTree(), StandardCharsets.UTF_8), StepTreeDTO.class);

            // 匹配结果
            Map<String, List<ApiScenarioReportResult>> maps = reportResults.stream().collect(Collectors.groupingBy(ApiScenarioReportResult::getResourceId));
            this.reportFormatting(stepList, maps);

            // 统计场景和步骤成功失败总量
            AtomicLong totalScenario = new AtomicLong();
            AtomicLong scenarioError = new AtomicLong();
            AtomicLong totalTime = new AtomicLong();

            calculate(stepList, totalScenario, scenarioError, totalTime);

            reportDTO.setTotalTime(totalTime.longValue());
            reportDTO.setScenarioTotal(totalScenario.longValue());
            reportDTO.setScenarioError(scenarioError.longValue());
            reportDTO.setScenarioSuccess((totalScenario.longValue() - scenarioError.longValue()));

            AtomicLong stepError = new AtomicLong();
            AtomicLong stepTotal = new AtomicLong();
            calculateStep(stepList, stepError, stepTotal);
            reportDTO.setScenarioStepTotal(stepTotal.longValue());
            reportDTO.setScenarioStepError(stepError.longValue());
            reportDTO.setScenarioStepSuccess((stepTotal.longValue() - stepError.longValue()));

            reportDTO.setConsole(scenarioReportStructure.getConsole());
            reportDTO.setSteps(stepList);

        }

        return reportDTO;
    }
}
