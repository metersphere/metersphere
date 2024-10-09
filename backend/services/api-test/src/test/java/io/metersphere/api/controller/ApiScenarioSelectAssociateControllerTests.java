package io.metersphere.api.controller;

import io.metersphere.api.domain.ApiDefinition;
import io.metersphere.api.domain.ApiDefinitionExample;
import io.metersphere.api.domain.ApiScenario;
import io.metersphere.api.domain.ApiScenarioExample;
import io.metersphere.api.dto.scenario.ApiScenarioSelectAssociateDTO;
import io.metersphere.api.dto.scenario.ApiScenarioStepDTO;
import io.metersphere.api.mapper.ApiDefinitionMapper;
import io.metersphere.api.mapper.ApiScenarioMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.ModuleSelectDTO;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ApiScenarioSelectAssociateControllerTests extends BaseTest {

    private static final String URL = "/api/scenario/associate/all";

    @Resource
    private ApiScenarioMapper apiScenarioMapper;
    @Resource
    private ApiDefinitionMapper apiDefinitionMapper;


    @Test
    @Order(0)
    public void testBatchExportReport() throws Exception {
        ApiScenarioExample apiScenarioExample = new ApiScenarioExample();
        apiScenarioExample.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID);
        List<ApiScenario> apiScenarios = apiScenarioMapper.selectByExample(apiScenarioExample);
        ApiScenario apiScenario = apiScenarios.get(0);


        ApiDefinitionExample apiDefinitionExample = new ApiDefinitionExample();
        apiDefinitionExample.createCriteria().andProjectIdEqualTo(DEFAULT_PROJECT_ID);
        List<ApiDefinition> apiDefinitionList = apiDefinitionMapper.selectByExample(apiDefinitionExample);
        ApiDefinition apiDefinition = apiDefinitionList.get(0);
        ApiDefinition apiDefinition1 = apiDefinitionList.get(1);

        ApiScenarioSelectAssociateDTO apiScenarioSelectAssociateDTO = new ApiScenarioSelectAssociateDTO();
        apiScenarioSelectAssociateDTO.setScenarioId(apiScenario.getId());
        apiScenarioSelectAssociateDTO.setProjectId(DEFAULT_PROJECT_ID);
        apiScenarioSelectAssociateDTO.setAssociateType("CASE");
        apiScenarioSelectAssociateDTO.setSelectAllModule(true);
        apiScenarioSelectAssociateDTO.setModuleMaps(new HashMap<>());
        apiScenarioSelectAssociateDTO.setRefType("REF");
        Map<String, ApiScenarioSelectAssociateDTO> requestMap = new HashMap<>();
        requestMap.put("CASE", apiScenarioSelectAssociateDTO);
        requestMap.put("SCENARIO",apiScenarioSelectAssociateDTO);
        requestMap.put("API",apiScenarioSelectAssociateDTO);
        MvcResult mvcResult = this.requestPostWithOkAndReturn(URL, requestMap);
        String returnData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(returnData, ResultHolder.class);
        List<ApiScenarioStepDTO> testCaseProviderDTOS = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), ApiScenarioStepDTO.class);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(testCaseProviderDTOS));
        Map<String, ModuleSelectDTO> moduleMaps = new HashMap<>();
        ModuleSelectDTO moduleSelectDTO = new ModuleSelectDTO();
        moduleSelectDTO.setSelectAll(false);
        moduleSelectDTO.setSelectIds(List.of(apiScenarios.get(0).getId()));
        moduleSelectDTO.setExcludeIds(List.of(apiScenarios.get(1).getId()));
        moduleMaps.put(apiScenarios.get(0).getModuleId(),moduleSelectDTO);
        apiScenarioSelectAssociateDTO.setSelectAllModule(false);
        apiScenarioSelectAssociateDTO.setModuleMaps(moduleMaps);
        apiScenarioSelectAssociateDTO.setProtocols(List.of("HTTP"));
        Map<String, ModuleSelectDTO> caseModuleMaps = new HashMap<>();
        ModuleSelectDTO caseModuleSelectDTO = new ModuleSelectDTO();
        caseModuleSelectDTO.setSelectAll(false);
        caseModuleSelectDTO.setSelectIds(List.of("sss"));
        caseModuleSelectDTO.setExcludeIds(List.of("fff"));
        caseModuleMaps.put(apiDefinitionList.get(3).getModuleId(),caseModuleSelectDTO);
        ApiScenarioSelectAssociateDTO caseScenarioSelectAssociateDTO = new ApiScenarioSelectAssociateDTO();
        caseScenarioSelectAssociateDTO.setScenarioId(apiScenario.getId());
        caseScenarioSelectAssociateDTO.setProjectId(DEFAULT_PROJECT_ID);
        caseScenarioSelectAssociateDTO.setAssociateType("CASE");
        caseScenarioSelectAssociateDTO.setProtocols(List.of("HTTP"));
        caseScenarioSelectAssociateDTO.setSelectAllModule(false);
        caseScenarioSelectAssociateDTO.setModuleMaps(caseModuleMaps);
        caseScenarioSelectAssociateDTO.setRefType("REF");
        Map<String, ModuleSelectDTO> apiModuleMaps = new HashMap<>();
        ModuleSelectDTO apiModuleSelectDTO = new ModuleSelectDTO();
        apiModuleSelectDTO.setSelectAll(false);
        apiModuleSelectDTO.setSelectIds(List.of(apiDefinition.getId()));
        apiModuleSelectDTO.setExcludeIds(List.of(apiDefinition1.getId()));
        apiModuleMaps.put(apiDefinitionList.get(3).getModuleId(),caseModuleSelectDTO);
        ApiScenarioSelectAssociateDTO apiDScenarioSelectAssociateDTO = new ApiScenarioSelectAssociateDTO();
        apiDScenarioSelectAssociateDTO.setScenarioId(apiScenario.getId());
        apiDScenarioSelectAssociateDTO.setProtocols(List.of("HTTP"));
        apiDScenarioSelectAssociateDTO.setProjectId(DEFAULT_PROJECT_ID);
        apiDScenarioSelectAssociateDTO.setAssociateType("CASE");
        apiDScenarioSelectAssociateDTO.setSelectAllModule(false);
        apiDScenarioSelectAssociateDTO.setModuleMaps(apiModuleMaps);
        apiDScenarioSelectAssociateDTO.setRefType("REF");
        requestMap = new HashMap<>();
        requestMap.put("CASE", apiScenarioSelectAssociateDTO);
        requestMap.put("SCENARIO",caseScenarioSelectAssociateDTO);
        requestMap.put("API",apiDScenarioSelectAssociateDTO);
        this.requestPostWithOkAndReturn(URL, requestMap);
    }

}
