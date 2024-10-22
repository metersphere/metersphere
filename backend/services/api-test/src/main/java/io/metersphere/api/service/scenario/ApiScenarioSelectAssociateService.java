package io.metersphere.api.service.scenario;

import io.metersphere.api.constants.ApiScenarioStepType;
import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioCsvStep;
import io.metersphere.api.domain.ApiTestCase;
import io.metersphere.api.dto.definition.ApiTestCaseAssociateDTO;
import io.metersphere.api.dto.scenario.ApiScenarioSelectAssociateDTO;
import io.metersphere.api.dto.scenario.ApiScenarioStepCommonDTO;
import io.metersphere.api.dto.scenario.ApiScenarioStepDTO;
import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.api.mapper.ExtApiScenarioMapper;
import io.metersphere.api.mapper.ExtApiScenarioStepMapper;
import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import io.metersphere.sdk.dto.AssociateCaseDTO;
import io.metersphere.system.dto.ModuleSelectDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApiScenarioSelectAssociateService {

    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;
    @Resource
    private ApiScenarioService apiScenarioService;
    @Resource
    private ExtApiScenarioStepMapper extApiScenarioStepMapper;


    public static final String MODULE_ALL = "all";


    public List<ApiScenarioStepDTO> getSelectDto(Map<String, ApiScenarioSelectAssociateDTO> request) {
        List<ApiScenarioStepDTO> steps = new ArrayList<>();
        for (String key : request.keySet()) {
            switch (key) {
                case "SCENARIO":
                    List<ApiScenarioStepDTO> apiScenarioStepDTOs = handleApiScenarioData(request.get(key));
                    steps.addAll(apiScenarioStepDTOs);
                    break;
                case "CASE":
                    List<ApiScenarioStepDTO> apiCaseStepDTOs = handleApiCaseData(request.get(key));
                    steps.addAll(apiCaseStepDTOs);
                    break;
                default:
                    List<ApiScenarioStepDTO> apiStepDTOs = handleApiData(request.get(key));
                    steps.addAll(apiStepDTOs);
                    break;
            }
        }
        return steps;
    }

    private List<ApiScenarioStepDTO> handleApiData(ApiScenarioSelectAssociateDTO request) {
        List<ApiScenarioStepDTO> steps = new ArrayList<>();
        boolean selectAllModule = request.isSelectAllModule();
        Map<String, ModuleSelectDTO> moduleMaps = request.getModuleMaps();
        moduleMaps.remove(MODULE_ALL);
        if (selectAllModule) {
            // 选择了全部模块
            List<ApiDefinition> apiDefinitionList = extApiDefinitionMapper.selectAllApi(request.getProjectId(), request.getProtocols());
            getApiSteps(request, apiDefinitionList, steps);
        } else {
            AssociateCaseDTO dto = getCaseIds(moduleMaps);
            List<ApiDefinition> apiDefinitionList = new ArrayList<>();
            //获取全选的模块数据
            if (CollectionUtils.isNotEmpty(dto.getModuleIds())) {
                apiDefinitionList = extApiDefinitionMapper.getListBySelectModules(request.getProjectId(), dto.getModuleIds(), request.getProtocols());
            }

            if (CollectionUtils.isNotEmpty(dto.getSelectIds())) {
                CollectionUtils.removeAll(dto.getSelectIds(), apiDefinitionList.stream().map(ApiDefinition::getId).toList());
                //获取选中的ids数据
                List<ApiDefinition> selectIdList = extApiDefinitionMapper.getListBySelectIds(request.getProjectId(), dto.getSelectIds(), request.getProtocols());
                apiDefinitionList.addAll(selectIdList);
            }
            if (CollectionUtils.isNotEmpty(dto.getExcludeIds())) {
                //排除的ids
                List<String> excludeIds = dto.getExcludeIds();
                apiDefinitionList = apiDefinitionList.stream().filter(item -> !excludeIds.contains(item.getId())).toList();
            }
            if (CollectionUtils.isNotEmpty(apiDefinitionList)) {
                List<ApiDefinition> list = apiDefinitionList.stream().sorted(Comparator.comparing(ApiDefinition::getPos)).toList();
                getApiSteps(request, list, steps);
            }

        }
        return steps;
    }

    private static void getApiSteps(ApiScenarioSelectAssociateDTO request, List<ApiDefinition> apiDefinitionList, List<ApiScenarioStepDTO> steps) {
        apiDefinitionList.forEach(item -> {
            ApiScenarioStepDTO step = new ApiScenarioStepDTO();
            LinkedHashMap<String, Object> config = new LinkedHashMap<>();
            config.put("enable", true);
            config.put("id", "");
            config.put("method", item.getMethod());
            config.put("name", "");
            config.put("protocol", item.getProtocol());
            step.setConfig(config);
            step.setStepType(ApiScenarioStepType.API.name());
            step.setName(item.getName());
            step.setResourceId(item.getId());
            step.setRefType(request.getRefType());
            step.setProjectId(item.getProjectId());
            step.setOriginProjectId(item.getProjectId());
            step.setResourceNum(item.getNum().toString());
            step.setVersionId(item.getVersionId());
            steps.add(step);
        });
    }

    private List<ApiScenarioStepDTO> handleApiCaseData(ApiScenarioSelectAssociateDTO request) {
        List<ApiScenarioStepDTO> steps = new ArrayList<>();
        boolean selectAllModule = request.isSelectAllModule();
        Map<String, ModuleSelectDTO> moduleMaps = request.getModuleMaps();
        moduleMaps.remove(MODULE_ALL);
        if (selectAllModule) {
            // 选择了全部模块
            List<ApiTestCaseAssociateDTO> apiTestCaseList = extApiTestCaseMapper.selectAllApiCaseWithAssociate(request.getProjectId(), request.getProtocols());
            getCaseSteps(request, apiTestCaseList, steps);
        } else {
            AssociateCaseDTO dto = getCaseIds(moduleMaps);
            List<ApiTestCaseAssociateDTO> apiTestCaseList = new ArrayList<>();
            //获取全选的模块数据
            if (CollectionUtils.isNotEmpty(dto.getModuleIds())) {
                apiTestCaseList = extApiTestCaseMapper.getListBySelectModulesWithAssociate(request.getProjectId(), dto.getModuleIds(), request.getProtocols());
            }

            if (CollectionUtils.isNotEmpty(dto.getSelectIds())) {
                CollectionUtils.removeAll(dto.getSelectIds(), apiTestCaseList.stream().map(ApiTestCase::getId).toList());
                //获取选中的ids数据
                List<ApiTestCaseAssociateDTO> selectIdList = extApiTestCaseMapper.getListBySelectIdsWithAssociate(request.getProjectId(), dto.getSelectIds(), request.getProtocols());
                apiTestCaseList.addAll(selectIdList);
            }

            if (CollectionUtils.isNotEmpty(dto.getExcludeIds())) {
                //排除的ids
                List<String> excludeIds = dto.getExcludeIds();
                apiTestCaseList = apiTestCaseList.stream().filter(item -> !excludeIds.contains(item.getId())).toList();
            }

            if (CollectionUtils.isNotEmpty(apiTestCaseList)) {
                List<ApiTestCaseAssociateDTO> list = apiTestCaseList.stream().sorted(Comparator.comparing(ApiTestCase::getPos)).toList();
                getCaseSteps(request, list, steps);
            }

        }

        return steps;
    }

    private static void getCaseSteps(ApiScenarioSelectAssociateDTO request, List<ApiTestCaseAssociateDTO> apiTestCaseList, List<ApiScenarioStepDTO> steps) {
        apiTestCaseList.forEach(item -> {
            ApiScenarioStepDTO step = new ApiScenarioStepDTO();
            step.setStepType(ApiScenarioStepType.API_CASE.name());
            LinkedHashMap<String, Object> config = new LinkedHashMap<>();
            config.put("enable", true);
            config.put("id", "");
            config.put("method", item.getMethod());
            config.put("name", "");
            config.put("protocol", item.getProtocol());
            step.setConfig(config);
            step.setName(item.getName());
            step.setResourceId(item.getId());
            step.setRefType(request.getRefType());
            step.setProjectId(item.getProjectId());
            step.setOriginProjectId(item.getProjectId());
            step.setResourceNum(item.getNum().toString());
            step.setVersionId(item.getVersionId());
            steps.add(step);
        });
    }

    private List<ApiScenarioStepDTO> handleApiScenarioData(ApiScenarioSelectAssociateDTO request) {
        List<ApiScenarioStepDTO> steps = new ArrayList<>();
        boolean selectAllModule = request.isSelectAllModule();
        Map<String, ModuleSelectDTO> moduleMaps = request.getModuleMaps();
        moduleMaps.remove(MODULE_ALL);
        if (selectAllModule) {
            // 选择了全部模块
            List<ApiScenario> scenarioList = extApiScenarioMapper.selectAllCaseExcludeSelf(request.getProjectId());
            getScenarioStepDto(request, scenarioList, steps);
        } else {
            AssociateCaseDTO dto = getCaseIds(moduleMaps);
            List<ApiScenario> scenarioList = new ArrayList<>();
            //获取全选的模块数据
            if (CollectionUtils.isNotEmpty(dto.getModuleIds())) {
                scenarioList = extApiScenarioMapper.getListBySelectModules(true, request.getProjectId(), dto.getModuleIds(), null);
            }
            if (CollectionUtils.isNotEmpty(dto.getSelectIds())) {
                CollectionUtils.removeAll(dto.getSelectIds(), scenarioList.stream().map(ApiScenario::getId).toList());
                //获取选中的ids数据
                List<ApiScenario> selectIdList = extApiScenarioMapper.getListBySelectIds(request.getProjectId(), dto.getSelectIds(), null);
                scenarioList.addAll(selectIdList);
            }
            List<String> excludeIds = new ArrayList<>();
            excludeIds.add(request.getScenarioId());
            if (CollectionUtils.isNotEmpty(dto.getExcludeIds())) {
                //排除的ids
                excludeIds.addAll(dto.getExcludeIds());
                scenarioList = scenarioList.stream().filter(item -> !excludeIds.contains(item.getId())).toList();
            }

            if (CollectionUtils.isNotEmpty(scenarioList)) {
                List<ApiScenario> list = scenarioList.stream().sorted(Comparator.comparing(ApiScenario::getPos)).toList();
                getScenarioStepDto(request, list, steps);
            }
        }
        return steps;
    }

    private void getScenarioStepDto(ApiScenarioSelectAssociateDTO request, List<ApiScenario> scenarioList, List<ApiScenarioStepDTO> steps) {
        List<String> ids = scenarioList.stream().map(ApiScenario::getId).toList();
        // 获取所有步骤
        List<ApiScenarioStepDTO> allSteps = apiScenarioService.getAllStepsByScenarioIds(ids)
                .stream()
                .distinct() // 这里可能存在多次引用相同场景，步骤可能会重复，去重
                .toList();

        // 设置步骤的 csvIds
        setStepCsvIds(ids, allSteps);

        // 构造 map，key 为场景ID，value 为步骤列表
        Map<String, List<ApiScenarioStepDTO>> scenarioStepMap = allSteps.stream()
                .collect(Collectors.groupingBy(step -> Optional.ofNullable(step.getScenarioId()).orElse(StringUtils.EMPTY)));

        // 查询步骤详情
        Map<String, String> stepDetailMap = apiScenarioService.getPartialRefStepDetailMap(allSteps);

        for (ApiScenario apiScenario : scenarioList) {
            List<ApiScenarioStepDTO> apiScenarioStepDTOS = scenarioStepMap.get(apiScenario.getId());
            List<ApiScenarioStepDTO> stepList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(apiScenarioStepDTOS)) {
                Map<String, List<ApiScenarioStepDTO>> currentScenarioParentStepMap = apiScenarioStepDTOS
                        .stream()
                        .collect(Collectors.groupingBy(step -> {
                            if (StringUtils.equals(step.getParentId(), "NONE")) {
                                step.setParentId(StringUtils.EMPTY);
                            }
                            return Optional.ofNullable(step.getParentId()).orElse(StringUtils.EMPTY);
                        }));

                stepList = apiScenarioService.buildStepTree(currentScenarioParentStepMap.get(StringUtils.EMPTY), currentScenarioParentStepMap, scenarioStepMap, new HashSet<>());
                // 设置部分引用的步骤的启用状态
                apiScenarioService.setPartialRefStepsEnable(stepList, stepDetailMap);
            }
            ApiScenarioStepDTO step = new ApiScenarioStepDTO();
            step.setStepType(ApiScenarioStepType.API_SCENARIO.name());
            step.setName(apiScenario.getName());
            step.setResourceId(apiScenario.getId());
            step.setRefType(request.getRefType());
            step.setProjectId(apiScenario.getProjectId());
            step.setOriginProjectId(apiScenario.getProjectId());
            step.setResourceNum(apiScenario.getNum().toString());
            step.setVersionId(apiScenario.getVersionId());
            step.setChildren(stepList);
            steps.add(step);
        }
    }

    protected AssociateCaseDTO getCaseIds(Map<String, ModuleSelectDTO> moduleMaps) {
        // 排除的ids
        List<String> excludeIds = moduleMaps.values().stream()
                .flatMap(moduleSelectDTO -> moduleSelectDTO.getExcludeIds().stream())
                .toList();
        // 选中的ids
        List<String> selectIds = moduleMaps.values().stream()
                .filter(moduleSelectDTO -> BooleanUtils.isFalse(moduleSelectDTO.isSelectAll()) && org.apache.commons.collections4.CollectionUtils.isNotEmpty(moduleSelectDTO.getSelectIds()))
                .flatMap(moduleSelectDTO -> moduleSelectDTO.getSelectIds().stream())
                .toList();
        // 全选的模块
        List<String> moduleIds = moduleMaps.entrySet().stream()
                .filter(entry -> BooleanUtils.isTrue(entry.getValue().isSelectAll()))
                .map(Map.Entry::getKey)
                .toList();

        return new AssociateCaseDTO(excludeIds, selectIds, moduleIds);
    }


    private void setStepCsvIds(List<String> scenarioIds, List<ApiScenarioStepDTO> allSteps) {
        List<String> refScenarioIds = allSteps.stream()
                .filter(apiScenarioService::isRefOrPartialScenario)
                .map(ApiScenarioStepCommonDTO::getResourceId)
                .collect(Collectors.toList());
        refScenarioIds.addAll(scenarioIds);

        //获取所有步骤的csv的关联关系
        List<ApiScenarioCsvStep> csvSteps = extApiScenarioStepMapper.getCsvStepByScenarioIds(refScenarioIds);
        // 构造 map，key 为步骤ID，value 为csv文件ID列表
        Map<String, List<String>> stepsCsvMap = csvSteps.stream()
                .collect(Collectors.groupingBy(ApiScenarioCsvStep::getStepId, Collectors.mapping(ApiScenarioCsvStep::getFileId, Collectors.toList())));

        //将stepsCsvMap根据步骤id放入到allSteps中
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(allSteps)) {
            allSteps.forEach(step -> step.setCsvIds(stepsCsvMap.get(step.getId())));
        }
    }


}
