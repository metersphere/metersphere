package io.metersphere.functional.controller;

import io.metersphere.functional.dto.FunctionalCaseAIDesignConfigDTO;
import io.metersphere.functional.dto.FunctionalCaseAIConfigDTO;
import io.metersphere.functional.dto.FunctionalCaseAITemplateConfigDTO;
import io.metersphere.system.base.BaseTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.Assert;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseAIControllerTests extends BaseTest {

    public static final String BASE_PATH = "/functional/case/ai";
    public static final String EDIT_CONFIG = "/save/config";
    public static final String GET_CONFIG = "/get/config";


    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }



    @Test
    @Order(0)
    public void testEdit() throws Exception {
        FunctionalCaseAIConfigDTO functionalCaseAIConfigDTO = new FunctionalCaseAIConfigDTO();
        FunctionalCaseAIDesignConfigDTO designPromptDTO = getFunctionalCaseAIDesignPromptDTO();
        functionalCaseAIConfigDTO.setDesignConfig(designPromptDTO);
        FunctionalCaseAITemplateConfigDTO templatePromptDTO = new FunctionalCaseAITemplateConfigDTO();
        templatePromptDTO.setCaseEditType("TEXT");
        templatePromptDTO.setCaseName(true);
        templatePromptDTO.setPreCondition(true);
        templatePromptDTO.setCaseSteps(true);
        templatePromptDTO.setExpectedResult(true);
        templatePromptDTO.setRemark(true);
        functionalCaseAIConfigDTO.setTemplateConfig(templatePromptDTO);
        this.requestPost(EDIT_CONFIG, functionalCaseAIConfigDTO).andExpect(status().isOk());
    }

    @NotNull
    private static FunctionalCaseAIDesignConfigDTO getFunctionalCaseAIDesignPromptDTO() {
        FunctionalCaseAIDesignConfigDTO designPromptDTO = new FunctionalCaseAIDesignConfigDTO();
        designPromptDTO.setAbnormal(true);
        designPromptDTO.setNormal(true);
        designPromptDTO.setEquivalenceClassPartitioning(true);
        designPromptDTO.setBoundaryValueAnalysis(true);
        designPromptDTO.setDecisionTableTesting(true);
        designPromptDTO.setCauseEffectGraphing(true);
        designPromptDTO.setOrthogonalExperimentMethod(true);
        designPromptDTO.setScenarioMethod(true);
        designPromptDTO.setScenarioMethodDescription("场景法描述");
        return designPromptDTO;
    }

    @Test
    @Order(1)
    public void testGet() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET_CONFIG);
        FunctionalCaseAIConfigDTO functionalCaseAIConfigDTO = getResultData(mvcResult, FunctionalCaseAIConfigDTO.class);
        Assert.notNull(functionalCaseAIConfigDTO, "获取AI提示词失败");
        System.out.println(functionalCaseAIConfigDTO);


    }

}
