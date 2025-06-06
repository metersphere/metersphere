package io.metersphere.functional.controller;

import io.metersphere.functional.dto.FunctionalCaseAIConfigDTO;
import io.metersphere.functional.dto.FunctionalCaseAIDesignConfigDTO;
import io.metersphere.functional.dto.FunctionalCaseAITemplateConfigDTO;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.dto.request.ai.AIChatRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.Assert;

import java.util.UUID;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class FunctionalCaseAIControllerTests extends BaseTest {

    public static final String BASE_PATH = "/functional/case/ai";
    public static final String EDIT_CONFIG = "/save/config";
    public static final String GET_CONFIG = "/get/config";
    private static final String TRANSFORM = "/transform";

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

    @Test
    @Order(3)
    public void transform() throws Exception {
        //String id = saveModel("DeepSeek-R1-Distill-Qwen-7B");
        AIChatRequest aiChatRequest = new AIChatRequest();
        aiChatRequest.setPrompt("# 登录中国平安网页 - 测试用例：登录成功验证\n" +
                "\n" +
                "## \uD83C\uDFAF 用例目标\n" +
                "验证用户使用正确的用户名和密码能够成功登录中国平安官网。\n" +
                "\n" +
                "---\n" +
                "\n" +
                "## 前置条件\n" +
                "\n" +
                "- 用户已注册中国平安账户，拥有有效的用户名和密码  \n" +
                "- 浏览器正常联网，且支持 JavaScript  \n" +
                "- 用户账户未被锁定，处于启用状态  \n" +
                "\n" +
                "---\n" +
                "\n" +
                "## 用例信息\n" +
                "\n" +
                "| 用例编号 | 用例名称             |\n" +
                "|----------|----------------------|\n" +
                "| TC001    | 输入正确的用户名和密码进行登录 |\n" +
                "\n" +
                "---\n" +
                "\n" +
                "## 测试步骤与预期结果\n" +
                "\n" +
                "| 步骤编号 | 操作步骤                                | 预期结果                            |\n" +
                "|----------|------------------------------------------|-------------------------------------|\n" +
                "| Step 1   | 打开中国平安官网                         | 网站首页正常加载                    |\n" +
                "| Step 2   | 点击右上角“登录”按钮                     | 页面跳转至登录页面                  |\n" +
                "| Step 3   | 在用户名输入框中输入正确用户名           | 系统正常接收输入                    |\n" +
                "| Step 4   | 在密码框中输入正确密码                   | 系统正常接收输入                    |\n" +
                "| Step 5   | 点击“登录”按钮                           | 登录请求发送，页面跳转至个人中心页面 |\n" +
                "\n" +
                "---\n" +
                "\n" +
                "## 备注\n" +
                "\n" +
                "- 本用例验证登录主流程的正确性  \n" +
                "- 建议与异常场景（如密码错误、验证码错误、账户锁定等）联合测试\n");
        aiChatRequest.setChatModelId("DeepSeek");
        aiChatRequest.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        aiChatRequest.setConversationId(UUID.randomUUID().toString());

        this.requestPost(TRANSFORM, aiChatRequest);

    }



}
