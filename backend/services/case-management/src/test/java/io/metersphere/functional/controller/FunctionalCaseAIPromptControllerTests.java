package io.metersphere.functional.controller;

import io.metersphere.functional.dto.FunctionalCaseAIDesignPromptDTO;
import io.metersphere.functional.dto.FunctionalCaseAIPromptDTO;
import io.metersphere.functional.dto.FunctionalCaseAITemplatePromptDTO;
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
public class FunctionalCaseAIPromptControllerTests extends BaseTest {

    public static final String BASE_PATH = "/functional/case/ai/prompt";
    public static final String EDIT_CONFIG = "/save";
    public static final String GET = "/get";


    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }



    @Test
    @Order(0)
    public void testEdit() throws Exception {
        FunctionalCaseAIPromptDTO functionalCaseAIPromptDTO = new FunctionalCaseAIPromptDTO();
        FunctionalCaseAIDesignPromptDTO designPromptDTO = getFunctionalCaseAIDesignPromptDTO();
        functionalCaseAIPromptDTO.setDesignPrompt(designPromptDTO);
        FunctionalCaseAITemplatePromptDTO templatePromptDTO = new FunctionalCaseAITemplatePromptDTO();
        templatePromptDTO.setTextDescription(true);
        templatePromptDTO.setStepDescription(false);
        templatePromptDTO.setCaseName(true);
        templatePromptDTO.setPreCondition(true);
        templatePromptDTO.setCaseSteps(true);
        templatePromptDTO.setExpectedResult(true);
        templatePromptDTO.setRemark(true);
        functionalCaseAIPromptDTO.setTemplatePrompt(templatePromptDTO);
        this.requestPost(EDIT_CONFIG, functionalCaseAIPromptDTO).andExpect(status().isOk());
    }

    @NotNull
    private static FunctionalCaseAIDesignPromptDTO getFunctionalCaseAIDesignPromptDTO() {
        FunctionalCaseAIDesignPromptDTO designPromptDTO = new FunctionalCaseAIDesignPromptDTO();
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
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET);
        FunctionalCaseAIPromptDTO functionalCaseAIPromptDTO = getResultData(mvcResult, FunctionalCaseAIPromptDTO.class);
        Assert.notNull(functionalCaseAIPromptDTO, "获取AI提示词失败");
        System.out.println(functionalCaseAIPromptDTO);


    }

}
