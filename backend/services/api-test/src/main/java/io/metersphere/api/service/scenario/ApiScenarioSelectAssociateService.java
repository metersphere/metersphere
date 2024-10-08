package io.metersphere.api.service.scenario;

import io.metersphere.api.mapper.ExtApiDefinitionMapper;
import io.metersphere.api.mapper.ExtApiScenarioMapper;
import io.metersphere.api.mapper.ExtApiTestCaseMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ApiScenarioSelectAssociateService {

    @Resource
    private ExtApiScenarioMapper extApiScenarioMapper;
    @Resource
    private ExtApiTestCaseMapper extApiTestCaseMapper;
    @Resource
    private ExtApiDefinitionMapper extApiDefinitionMapper;

    public static final String MODULE_ALL = "all";


    /*public void getSelectDto(ApiScenarioSelectAssociateDTO request) {
        boolean selectAllModule = request.isSelectAllModule();
        Map<String, ModuleSelectDTO> moduleMaps = request.getModuleMaps();
        moduleMaps.remove(MODULE_ALL);

        switch (request.getAssociateType()) {
            case "API_SCENARIO":
                handleApiScenarioData(selectAllModule, moduleMaps, request);
            case "API_CASE":
                handleApiCaseData(selectAllModule, moduleMaps, request);
            default:
                handleApiData(selectAllModule, moduleMaps, request);
        }

    }

    private void handleApiData(boolean selectAllModule, Map<String, ModuleSelectDTO> moduleMaps, ApiScenarioSelectAssociateDTO request) {
        if (selectAllModule) {
            // 选择了全部模块
            List<ApiDefinition> apiDefinitionList = extApiDefinitionMapper.selectAllApi(request.getProjectId(), request.getProtocols());
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
                List<ApiDefinition> selectIdList = extApiDefinitionMapper.getListBySelectIds(dto.getSelectIds(), request.getProtocols());
                apiDefinitionList.addAll(selectIdList);
            }

            if (CollectionUtils.isNotEmpty(dto.getExcludeIds())) {
                //排除的ids
                List<String> excludeIds = dto.getExcludeIds();
                apiDefinitionList = apiDefinitionList.stream().filter(item -> !excludeIds.contains(item.getId())).toList();
            }

            if (CollectionUtils.isNotEmpty(apiDefinitionList)) {
                List<ApiDefinition> list = apiDefinitionList.stream().sorted(Comparator.comparing(ApiDefinition::getPos)).toList();
            }

        }
    }

    private void handleApiCaseData(boolean selectAllModule, Map<String, ModuleSelectDTO> moduleMaps, ApiScenarioSelectAssociateDTO request) {
        if (selectAllModule) {
            // 选择了全部模块
            List<ApiTestCase> apiTestCaseList = extApiTestCaseMapper.selectAllApiCase(true, request.getProjectId(), null, request.getProtocols());
        } else {
            AssociateCaseDTO dto = getCaseIds(moduleMaps);
            List<ApiTestCase> apiTestCaseList = new ArrayList<>();
            //获取全选的模块数据
            if (CollectionUtils.isNotEmpty(dto.getModuleIds())) {
                apiTestCaseList = extApiTestCaseMapper.getListBySelectModules(true, request.getProjectId(), dto.getModuleIds(), null, request.getProtocols());
            }

            if (CollectionUtils.isNotEmpty(dto.getSelectIds())) {
                CollectionUtils.removeAll(dto.getSelectIds(), apiTestCaseList.stream().map(ApiTestCase::getId).toList());
                //获取选中的ids数据
                List<ApiTestCase> selectIdList = extApiTestCaseMapper.getListBySelectIds(request.getProjectId(), dto.getSelectIds(), null, request.getProtocols());
                apiTestCaseList.addAll(selectIdList);
            }

            if (CollectionUtils.isNotEmpty(dto.getExcludeIds())) {
                //排除的ids
                List<String> excludeIds = dto.getExcludeIds();
                apiTestCaseList = apiTestCaseList.stream().filter(item -> !excludeIds.contains(item.getId())).toList();
            }

            if (CollectionUtils.isNotEmpty(apiTestCaseList)) {
                List<ApiTestCase> list = apiTestCaseList.stream().sorted(Comparator.comparing(ApiTestCase::getPos)).toList();
            }

        }
    }

    private void handleApiScenarioData(boolean selectAllModule, Map<String, ModuleSelectDTO> moduleMaps, ApiScenarioSelectAssociateDTO request) {
        if (selectAllModule) {
            // 选择了全部模块
            List<ApiScenario> scenarioList = extApiScenarioMapper.selectAllCase(true, request.getProjectId(), null);
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

            if (CollectionUtils.isNotEmpty(dto.getExcludeIds())) {
                //排除的ids
                List<String> excludeIds = dto.getExcludeIds();
                excludeIds.add(request.getScenarioId());
                scenarioList = scenarioList.stream().filter(item -> !excludeIds.contains(item.getId())).toList();
            }

            if (CollectionUtils.isNotEmpty(scenarioList)) {
                List<ApiScenario> list = scenarioList.stream().sorted(Comparator.comparing(ApiScenario::getPos)).toList();
            }

        }
    }

    *//**
     * 获取关联时的相关id数据
     *//*
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
    }*/

}
