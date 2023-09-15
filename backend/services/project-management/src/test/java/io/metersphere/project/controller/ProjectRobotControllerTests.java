package io.metersphere.project.controller;

import io.metersphere.project.domain.ProjectRobot;
import io.metersphere.project.dto.ProjectRobotDTO;
import io.metersphere.project.enums.ProjectRobotPlatform;
import io.metersphere.project.enums.ProjectRobotType;
import io.metersphere.project.request.ProjectRobotRequest;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Pager;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectRobotControllerTests extends BaseTest {

    public static final String ROBOT_ADD = "/project/robot/add";

    public static final String ROBOT_UPDATE = "/project/robot/update";

    public static final String ROBOT_DELETE = "/project/robot/delete";

    public static final String ROBOT_ENABLE = "/project/robot/enable";

    public static final String ROBOT_LIST = "/project/robot/list/page";

    public static final String ROBOT_DETAIL = "/project/robot/get";


    @Test
    @Order(1)
    void addRobotSuccessWeCom() throws Exception {
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName("企业微信机器人");
        projectRobotDTO.setPlatform(ProjectRobotPlatform.WE_COM.toString());
        projectRobotDTO.setProjectId("test_project");
        projectRobotDTO.setWebhook("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=2b67ccf4-e0da-4cd6-ae74-8d42657865f8");
        getPostResult(projectRobotDTO, ROBOT_ADD, status().isOk());
        listByKeyWord("企业微信机器人");

    }

    @Test
    @Order(2)
    void addRobotSuccessLark() throws Exception {
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName("飞书机器人");
        projectRobotDTO.setPlatform(ProjectRobotPlatform.LARK.toString());
        projectRobotDTO.setProjectId("test_project");
        projectRobotDTO.setWebhook("https://open.feishu.cn/open-apis/bot/v2/hook/a6024229-9d9d-41c2-8662-7bc3da1092cb");
        getPostResult(projectRobotDTO, ROBOT_ADD, status().isOk());
        listByKeyWord("飞书机器人");
    }

    @Test
    @Order(3)
    void addRobotSuccessDingCustom() throws Exception {
        setDingCustom("钉钉自定义机器人");
        listByKeyWord("钉钉自定义机器人");
    }

    private void setDingCustom(String name) throws Exception {
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName(name);
        projectRobotDTO.setPlatform(ProjectRobotPlatform.DING_TALK.toString());
        projectRobotDTO.setProjectId("test_project");
        projectRobotDTO.setType(ProjectRobotType.CUSTOM.toString());
        projectRobotDTO.setWebhook("https://oapi.dingtalk.com/robot/send?access_token=fd963136a4d7eebaaa68de261223089148e62d7519fbaf426626fe3157725b8a");
        getPostResult(projectRobotDTO, ROBOT_ADD, status().isOk());
    }

    @Test
    @Order(4)
    void addRobotSuccessDingEn() throws Exception {
        setDingEn("钉钉企业应用机器人");
        listByKeyWord("钉钉企业应用机器人");
    }

    private void setDingEn(String name) throws Exception {
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName(name);
        projectRobotDTO.setPlatform(ProjectRobotPlatform.DING_TALK.toString());
        projectRobotDTO.setProjectId("test_project");
        projectRobotDTO.setType(ProjectRobotType.ENTERPRISE.toString());
        projectRobotDTO.setAppKey("dingxwd71o7kj4qoixo7");
        projectRobotDTO.setAppSecret("szmOD9bjGgKtfYk09-Xx2rPdX-xkW4R8Iic0eig_k1D3k95nG4TLKRSpUKUD_f0G");
        projectRobotDTO.setWebhook("https://oapi.dingtalk.com/robot/send?access_token=e971f376669334cd44c585d419f0fdfa1600f97f906109b377999d8a0986b11e");
        getPostResult(projectRobotDTO, ROBOT_ADD, status().isOk());
    }

    @Test
    @Order(5)
    void addRobotFailDingCustomByType() throws Exception {
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName("钉钉自定义机器人");
        projectRobotDTO.setPlatform(ProjectRobotPlatform.DING_TALK.toString());
        projectRobotDTO.setProjectId("test_project");
        projectRobotDTO.setWebhook("https://oapi.dingtalk.com/robot/send?access_token=e971f376669334cd44c585d419f0fdfa1600f97f906109b377999d8a0986b11e");
        getPostResult(projectRobotDTO, ROBOT_ADD, status().is5xxServerError());
    }

    @Test
    @Order(6)
    void addRobotFailDingCustomByKey() throws Exception {
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName("钉钉自定义机器人");
        projectRobotDTO.setPlatform(ProjectRobotPlatform.DING_TALK.toString());
        projectRobotDTO.setProjectId("test_project");
        projectRobotDTO.setType(ProjectRobotType.ENTERPRISE.toString());
        projectRobotDTO.setWebhook("https://oapi.dingtalk.com/robot/send?access_token=e971f376669334cd44c585d419f0fdfa1600f97f906109b377999d8a0986b11e");
        getPostResult(projectRobotDTO, ROBOT_ADD, status().is5xxServerError());
    }

    @Test
    @Order(7)
    void addRobotFailDingCustomBySecret() throws Exception {
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName("钉钉自定义机器人");
        projectRobotDTO.setPlatform(ProjectRobotPlatform.DING_TALK.toString());
        projectRobotDTO.setProjectId("test_project");
        projectRobotDTO.setType(ProjectRobotType.ENTERPRISE.toString());
        projectRobotDTO.setAppKey("dingxwd71o7kj4qoixo7");
        projectRobotDTO.setWebhook("https://oapi.dingtalk.com/robot/send?access_token=e971f376669334cd44c585d419f0fdfa1600f97f906109b377999d8a0986b11e");
        getPostResult(projectRobotDTO, ROBOT_ADD, status().is5xxServerError());
    }

    @Test
    @Order(8)
    void updateRobotSuccessCusTom() throws Exception {
        setCustomRobot("用于更新自定义机器人");
        ProjectRobot projectRobot = getRobot("用于更新自定义机器人");
        checkUpdate(projectRobot, "更新自定义机器人", status().isOk());
    }

    @Test
    @Order(9)
    void updateRobotSuccessDingCus() throws Exception {
        setDingCustom("用于更新钉钉自定义机器人");
        ProjectRobot projectRobot = getRobot("用于更新钉钉自定义机器人");
        checkUpdate(projectRobot, "更新钉钉自定义机器人", status().isOk());
    }

    @Test
    @Order(10)
    void updateRobotSuccessDingEn() throws Exception {
        setDingEn("用于更新钉钉企业机器人");
        ProjectRobot projectRobot = getRobot("用于更新钉钉企业机器人");
        checkUpdate(projectRobot, "更新钉钉企业机器人", status().isOk());
    }

    @Test
    @Order(11)
    void updateRobotFileIdNotExist() throws Exception {
        setCustomRobot("测试没有ID失败");
        ProjectRobot projectRobot = getRobot("测试没有ID失败");
        projectRobot.setId("noId");
        checkUpdate(projectRobot, "测试没有ID失败", status().is5xxServerError());
    }

    @Test
    @Order(12)
    void updateRobotFileIdNoId() throws Exception {
        setCustomRobot("测试ID空失败");
        ProjectRobot projectRobot = getRobot("测试ID空失败");
        projectRobot.setId(null);
        checkUpdate(projectRobot, "测试ID空失败", status().isBadRequest());
    }

    @Test
    @Order(13)
    void updateRobotFileNoDingType() throws Exception {
        setDingCustom("测试更新没有Type失败");
        ProjectRobot projectRobot = getRobot("测试更新没有Type失败");
        projectRobot.setType(null);
        checkUpdate(projectRobot, "测试更新没有Type失败", status().is5xxServerError());
    }

    @Test
    @Order(14)
    void updateRobotFileNoDingKey() throws Exception {
        setDingEn("测试更新没有key失败");
        ProjectRobot projectRobot = getRobot("测试更新没有key失败");
        projectRobot.setAppKey(null);
        checkUpdate(projectRobot, "测试更新没有key失败", status().is5xxServerError());
    }

    @Test
    @Order(15)
    void updateRobotFileNoDingSecret() throws Exception {
        setDingEn("测试更新没有Secret失败");
        ProjectRobot projectRobot = getRobot("测试更新没有Secret失败");
        projectRobot.setAppSecret(null);
        checkUpdate(projectRobot, "测试更新没有Secret失败", status().is5xxServerError());
    }

    @Test
    @Order(16)
    void deleteRobotSuccess() throws Exception {
        setCustomRobot("测试删除");
        ProjectRobot projectRobot = getRobot("测试删除");
        String projectRobotId = projectRobot.getId();
        mockMvc.perform(MockMvcRequestBuilders.get(ROBOT_DELETE + "/" + projectRobotId)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        mockMvc.perform(MockMvcRequestBuilders.get(ROBOT_DETAIL + "/" + projectRobotId)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }


    @Test
    @Order(17)
    void deleteRobotFail() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ROBOT_DELETE + "/no_id")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(18)
    void getDetailFail() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ROBOT_DETAIL + "/no_id")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(16)
    void getDetailSuccess() throws Exception {
        setCustomRobot("测试获取详情");
        ProjectRobot projectRobot = getRobot("测试获取详情");
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(ROBOT_DETAIL + "/" + projectRobot.getId())
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        ProjectRobot result = getResult(mvcResult);
        Assertions.assertTrue(StringUtils.equals(result.getName(), "测试获取详情"));
        Assertions.assertTrue(StringUtils.equals(result.getWebhook(), "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=2b67ccf4-e0da-4cd6-ae74-8d42657865f8"));
    }

    @Test
    @Order(17)
    void getListSuccessNoKeyword() throws Exception {
        ProjectRobotRequest request = new ProjectRobotRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        Pager<?> sortPageData = getPager(request);
        List<ProjectRobot> projectRobots = JSON.parseArray(JSON.toJSONString(sortPageData.getList()), ProjectRobot.class);
        Assertions.assertTrue(projectRobots.size() > 0);
    }

    @Test
    @Order(18)
    void getListSuccessEnable() throws Exception {
        setCustomRobot("测试集合");
        ProjectRobotRequest request = new ProjectRobotRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        request.setKeyword("测试集合");
        request.setEnable(true);
        Pager<?> sortPageData = getPager(request);
        List<ProjectRobot> projectRobots = JSON.parseArray(JSON.toJSONString(sortPageData.getList()), ProjectRobot.class);
        ProjectRobot projectRobot = projectRobots.get(0);
        Assertions.assertTrue(projectRobot.getEnable());
    }

    @Test
    @Order(19)
    void getListSuccessByProject() throws Exception {
        setCustomRobot("测试集合");
        ProjectRobotRequest request = new ProjectRobotRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        request.setProjectId("test_project");
        request.setEnable(true);
        Pager<?> sortPageData = getPager(request);
        List<ProjectRobot> projectRobots = JSON.parseArray(JSON.toJSONString(sortPageData.getList()), ProjectRobot.class);
        ProjectRobot projectRobot = projectRobots.get(0);
        Assertions.assertTrue(projectRobot.getEnable());
    }

    @Test
    @Order(20)
    void setEnableSuccess() throws Exception {
        setCustomRobot("测试Enable");
        ProjectRobot projectRobot = getRobot("测试Enable");
        String projectRobotId = projectRobot.getId();
        mockMvc.perform(MockMvcRequestBuilders.get(ROBOT_ENABLE + "/" + projectRobotId)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        ProjectRobot projectRobotEnable = getRobot("测试Enable");
        Assertions.assertFalse(projectRobotEnable.getEnable());
    }

    @Test
    @Order(21)
    void setEnableFalseSuccess() throws Exception {
        ProjectRobot projectRobot = getRobot("测试Enable");
        String projectRobotId = projectRobot.getId();
        mockMvc.perform(MockMvcRequestBuilders.get(ROBOT_ENABLE + "/" + projectRobotId)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        ProjectRobot projectRobotEnable = getRobot("测试Enable");
        Assertions.assertTrue(projectRobotEnable.getEnable());
    }

    @Test
    @Order(22)
    void setEnableFail() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ROBOT_ENABLE + "/no_id")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }



    private static ProjectRobot getResult(MvcResult mvcResult) throws UnsupportedEncodingException {
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ;
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ProjectRobot.class);
    }


    private void checkUpdate(ProjectRobot projectRobot, String name, ResultMatcher resultMatcher) throws Exception {
        projectRobot.setName(name);
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        BeanUtils.copyBean(projectRobotDTO, projectRobot);
        getPostResult(projectRobotDTO, ROBOT_UPDATE, resultMatcher);
        if (resultMatcher.equals(status().isOk())) {
            ProjectRobot projectRobotUpdate = getRobot(name);
            Assertions.assertTrue(StringUtils.equals(projectRobotUpdate.getName(), name));
        }
    }

    private void setCustomRobot(String name) throws Exception {
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName(name);
        projectRobotDTO.setPlatform(ProjectRobotPlatform.CUSTOM.toString());
        projectRobotDTO.setProjectId("test_project");
        projectRobotDTO.setWebhook("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=2b67ccf4-e0da-4cd6-ae74-8d42657865f8");
        getPostResult(projectRobotDTO, ROBOT_ADD, status().isOk());
    }

    private void getPostResult(ProjectRobotDTO projectRobotDTO, String url, ResultMatcher resultMatcher) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(projectRobotDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    void listByKeyWord(String keyWord) throws Exception {
        ProjectRobot projectRobot = getRobot(keyWord);
        Assertions.assertTrue(StringUtils.equals(projectRobot.getName(), keyWord));
    }

    private ProjectRobot getRobot(String keyWord) throws Exception {
        ProjectRobotRequest request = new ProjectRobotRequest();
        request.setCurrent(1);
        request.setPageSize(5);
        request.setKeyword(keyWord);
        Pager<?> sortPageData = getPager(request);
        ProjectRobot projectRobot = JSON.parseArray(JSON.toJSONString(sortPageData.getList()), ProjectRobot.class).get(0);
        return projectRobot;
    }

    private Pager<?> getPager(ProjectRobotRequest request) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(ROBOT_LIST)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder sortHolder = JSON.parseObject(sortData, ResultHolder.class);
        Pager<?> sortPageData = JSON.parseObject(JSON.toJSONString(sortHolder.getData()), Pager.class);
        return sortPageData;
    }


}
