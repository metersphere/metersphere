package io.metersphere.functional.controller;

import io.metersphere.ai.engine.common.AIModelType;
import io.metersphere.functional.dto.FunctionalCaseAIConfigDTO;
import io.metersphere.functional.dto.FunctionalCaseAIDesignConfigDTO;
import io.metersphere.functional.dto.FunctionalCaseAITemplateConfigDTO;
import io.metersphere.functional.request.FunctionalCaseAIChatRequest;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.constants.AIConfigConstants;
import io.metersphere.system.domain.AiModelSource;
import io.metersphere.system.dto.request.ai.AIChatRequest;
import io.metersphere.system.mapper.AiModelSourceMapper;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.Assert;

import java.util.ArrayList;
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
    private static final String BATCH_SAVE = "/batch/save";

    @Override
    protected String getBasePath() {
        return BASE_PATH;
    }

    @Resource
    private AiModelSourceMapper aiModelSourceMapper;

    @Test
    @Order(0)
    public void testEdit() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GET_CONFIG);
        FunctionalCaseAIConfigDTO functionalCaseAIConfigDTO = getResultData(mvcResult, FunctionalCaseAIConfigDTO.class);
        Assert.notNull(functionalCaseAIConfigDTO, "获取AI提示词失败");
        System.out.println(functionalCaseAIConfigDTO);
        functionalCaseAIConfigDTO = new FunctionalCaseAIConfigDTO();
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
        designPromptDTO.setAbnormal(false);
        designPromptDTO.setScenarioMethod(false);
        designPromptDTO.setScenarioMethodDescription(null);
        functionalCaseAIConfigDTO.setDesignConfig(designPromptDTO);
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

    @Test
    @Order(4)
    public void setBatchSave() throws Exception {
        String id = saveModel("DeepSeek-R1-Distill-Qwen-7B");
        FunctionalCaseAIChatRequest aiChatRequest = new FunctionalCaseAIChatRequest();
        aiChatRequest.setPrompt("# 登录中国平安网页 - 测试用例文档\n" +
                "\n" +
                "## \uD83C\uDFAF 测试目标\n" +
                "\n" +
                "验证中国平安网页登录功能的正确性、安全性及稳定性，包括正常登录流程与各类异常场景。\n" +
                "\n" +
                "---\n" +
                "\n" +
                "## 前置条件\n" +
                "\n" +
                "- 用户拥有有效的中国平安账户（用户名/密码）\n" +
                "- 用户设备可正常联网\n" +
                "- 支持的浏览器（如 Chrome、Edge）已安装并更新到最新版本\n" +
                "- 清除浏览器缓存或启用无痕模式避免缓存干扰\n" +
                "\n" +
                "---\n" +
                "\n" +
                "## 测试用例表格\n" +
                "\n" +
                "| 用例编号 | 步骤描述                                     | 输入数据                          | 预期结果                                                         |\n" +
                "|----------|----------------------------------------------|-----------------------------------|------------------------------------------------------------------|\n" +
                "| TC001    | 打开中国平安官网首页                         | 无                                | 成功加载官网页面，无异常提示                                     |\n" +
                "| TC002    | 点击“登录”按钮，跳转到登录页                 | 无                                | 页面跳转至登录页，显示用户名与密码输入框                         |\n" +
                "| TC003    | 输入正确用户名与密码点击“登录”              | 用户名：valid_user<br>密码：valid_pass | 登录成功，跳转至用户个人中心页面                                |\n" +
                "| TC004    | 输入错误密码点击“登录”                      | 用户名：valid_user<br>密码：wrong_pass | 提示“用户名或密码错误”，停留在登录页                            |\n" +
                "| TC005    | 输入不存在的用户名点击“登录”                | 用户名：non_exist<br>密码：any_pass | 提示“用户名不存在”或“用户名或密码错误”                         |\n" +
                "| TC006    | 用户名或密码为空点击“登录”                  | 用户名：空<br>密码：any_pass 或反之 | 提示“请输入用户名/密码”                                        |\n" +
                "| TC007    | 连续输错密码达到系统限制次数                 | 错误密码连续5次                    | 提示“账户已锁定/稍后再试”，禁止继续尝试登录                     |\n" +
                "| TC008    | 登录过程中网络断开                           | 登录时断网                         | 页面提示“网络连接异常”或请求失败                                 |\n" +
                "| TC009    | 浏览器禁用JavaScript                         | 浏览器禁用JS                       | 页面可能无法加载登录框或按钮不可用，提示用户启用JavaScript      |\n" +
                "| TC010    | 模拟CSRF/XSS攻击尝试提交恶意脚本              | 用户名字段输入：<script>alert(1)</script> | 系统应进行安全过滤，禁止脚本执行                                |\n" +
                "| TC011    | 登录页验证码功能验证（如存在）               | 输入错误验证码                    | 提示“验证码错误”，不可登录                                      |\n" +
                "| TC012    | 多端登录检测（如系统限制）                   | 同时在两台设备使用同账号登录       | 若有限制，应提示“账号已在其他设备登录”或踢下线策略生效         |\n" +
                "| TC013    | 登录成功后尝试返回登录页                     | 已登录后访问登录页                 | 自动跳转至首页或个人中心，避免重复登录页面显示                 |\n" +
                "\n" +
                "---\n" +
                "\n" +
                "## 备注\n" +
                "\n" +
                "- 验证码、二次身份验证或短信验证等安全措施，如存在，应纳入扩展测试场景。\n" +
                "- 安全测试必须包括 XSS、CSRF、SQL 注入的基本尝试。\n" +
                "- 多终端并发登录行为和会话过期机制（如超时自动退出）建议纳入系统性测试。\n" +
                "- 可结合性能测试工具对登录接口进行压力测试、并发登录测试。\n" +
                "\n");
        aiChatRequest.setChatModelId("DeepSeek");
        aiChatRequest.setOrganizationId(DEFAULT_ORGANIZATION_ID);
        aiChatRequest.setConversationId(UUID.randomUUID().toString());
        aiChatRequest.setModuleId("100345");
        aiChatRequest.setProjectId(DEFAULT_PROJECT_ID);
        aiChatRequest.setTemplateId("1001");
        aiChatRequest.setChatModelId(id);
        this.requestPost(BATCH_SAVE, aiChatRequest);

    }

    private String saveModel(String name){
        String id = IDGenerator.nextStr();
        AiModelSource aiModelSource = new AiModelSource();
        aiModelSource.setId(id);
        aiModelSource.setType("LLM");
        aiModelSource.setName(name);
        aiModelSource.setProviderName(AIModelType.DEEP_SEEK);
        aiModelSource.setPermissionType(AIConfigConstants.AiPermissionType.PUBLIC.toString());
        aiModelSource.setOwnerType(AIConfigConstants.AiOwnerType.ORGANIZATION.toString());
        aiModelSource.setBaseName("deepseek-ai/DeepSeek-R1-Distill-Qwen-7B");
        aiModelSource.setApiUrl("https://api.siliconflow.cn");
        aiModelSource.setAppKey("sk-eaglaphcsokapygezyovozbzaypm");
        aiModelSource.setStatus(true);
        aiModelSource.setOwner("admin");
        aiModelSource.setCreateTime(System.currentTimeMillis());
        aiModelSource.setCreateUser("admin");
        aiModelSource.setAdvSettings(JSON.toJSONString(new ArrayList<>()));
        aiModelSourceMapper.insert(aiModelSource);
        return id;
    }


}
