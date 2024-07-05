package io.metersphere.project.controller;

import io.metersphere.project.domain.*;
import io.metersphere.project.dto.MessageTaskDTO;
import io.metersphere.project.dto.MessageTaskDetailDTO;
import io.metersphere.project.dto.MessageTaskTypeDTO;
import io.metersphere.project.dto.ProjectRobotDTO;
import io.metersphere.project.mapper.MessageTaskBlobMapper;
import io.metersphere.project.mapper.MessageTaskMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.mapper.ProjectRobotMapper;
import io.metersphere.sdk.constants.SessionConstants;
import io.metersphere.sdk.domain.OperationLogExample;
import io.metersphere.sdk.mapper.OperationLogMapper;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.MessageTaskRequest;
import io.metersphere.system.log.constants.OperationLogType;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.*;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectRobotControllerTests extends BaseTest {

    public static final String ROBOT_ADD = "/project/robot/add";

    public static final String ROBOT_UPDATE = "/project/robot/update";

    public static final String ROBOT_DELETE = "/project/robot/delete";

    public static final String ROBOT_ENABLE = "/project/robot/enable";

    public static final String ROBOT_LIST = "/project/robot/list/";

    public static final String ROBOT_DETAIL = "/project/robot/get";

    @Resource
    private ProjectRobotMapper projectRobotMapper;

    @Resource
    private ProjectMapper projectMapper;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Resource
    private OperationLogMapper operationLogMapper;


    @Test
    @Order(1)
    void addRobotSuccessWeCom() throws Exception {
        ProjectRobotDTO projectRobotDTOInSite = new ProjectRobotDTO();
        projectRobotDTOInSite.setName("robot_in_site");
        projectRobotDTOInSite.setPlatform(NoticeConstants.Type.IN_SITE);
        projectRobotDTOInSite.setProjectId("test_project");
        projectRobotDTOInSite.setWebhook("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=2b67ccf4-e0da-4cd6-ae74-8d42657865f8");
        getPostResult(projectRobotDTOInSite, ROBOT_ADD, status().isOk());
        ProjectRobotDTO projectRobotDTOMail = new ProjectRobotDTO();
        projectRobotDTOMail.setName("robot_mail");
        projectRobotDTOMail.setPlatform(NoticeConstants.Type.MAIL);
        projectRobotDTOMail.setProjectId("test_project");
        projectRobotDTOMail.setWebhook("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=2b67ccf4-e0da-4cd6-ae74-8d42657865f8");
        getPostResult(projectRobotDTOMail, ROBOT_ADD, status().isOk());
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName("企业微信机器人");
        projectRobotDTO.setPlatform(NoticeConstants.Type.WE_COM);
        projectRobotDTO.setProjectId("test_project");
        projectRobotDTO.setWebhook("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=2b67ccf4-e0da-4cd6-ae74-8d42657865f8");
        getPostResult(projectRobotDTO, ROBOT_ADD, status().isOk());
        checkName("test_project", "企业微信机器人");
    }

    private void checkName(String projectId, String name) throws Exception {
        List<ProjectRobot> testProject = getList(projectId);
        List<String> nameList = testProject.stream().map(ProjectRobot::getName).toList();
        Assertions.assertTrue(nameList.contains(name));
    }

    @Test
    @Order(2)
    void addRobotSuccessLark() throws Exception {
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName("飞书机器人");
        projectRobotDTO.setPlatform(NoticeConstants.Type.LARK);
        projectRobotDTO.setProjectId("test_project");
        projectRobotDTO.setWebhook("https://open.feishu.cn/open-apis/bot/v2/hook/a6024229-9d9d-41c2-8662-7bc3da1092cb");
        ProjectRobot postResult = getPostResult(projectRobotDTO, ROBOT_ADD, status().isOk());
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(postResult.getPlatform(),NoticeConstants.Type.LARK));
        checkName("test_project", "飞书机器人");
        ProjectRobot robot = getRobot("test_project", "飞书机器人");
        checkContentLog(robot.getName(),OperationLogType.ADD);
    }

    @Test
    @Order(3)
    void addRobotSuccessDingCustom() throws Exception {
        ProjectRobotDTO projectRobotDTOInSite = new ProjectRobotDTO();
        projectRobotDTOInSite.setName("robot_in_site");
        projectRobotDTOInSite.setPlatform(NoticeConstants.Type.IN_SITE);
        projectRobotDTOInSite.setProjectId("test_project3");
        projectRobotDTOInSite.setWebhook("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=2b67ccf4-e0da-4cd6-ae74-8d42657865f8");
        getPostResult(projectRobotDTOInSite, ROBOT_ADD, status().isOk());
        ProjectRobotDTO projectRobotDTOMail = new ProjectRobotDTO();
        projectRobotDTOMail.setName("robot_mail");
        projectRobotDTOMail.setPlatform(NoticeConstants.Type.MAIL);
        projectRobotDTOMail.setProjectId("test_project3");
        projectRobotDTOMail.setWebhook("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=2b67ccf4-e0da-4cd6-ae74-8d42657865f8");
        getPostResult(projectRobotDTOMail, ROBOT_ADD, status().isOk());
        setDingCustom("test_project3","钉钉自定义机器人4");
        checkName("test_project3", "钉钉自定义机器人4");
    }

    private void setInSiteCustom(String projectId,String name) throws Exception {
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName(name);
        projectRobotDTO.setPlatform(NoticeConstants.Type.IN_SITE);
        projectRobotDTO.setProjectId(projectId);
        projectRobotDTO.setEnable(true);
        projectRobotDTO.setWebhook("NONE");
        getPostResult(projectRobotDTO, ROBOT_ADD, status().isOk());
    }

    private void setDingCustom(String projectId,String name) throws Exception {
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName(name);
        projectRobotDTO.setPlatform(NoticeConstants.Type.DING_TALK);
        projectRobotDTO.setProjectId(projectId);
        projectRobotDTO.setEnable(true);
        projectRobotDTO.setType(NoticeConstants.DingType.CUSTOM);
        projectRobotDTO.setWebhook("https://oapi.dingtalk.com/robot/send?access_token=fd963136a4d7eebaaa68de261223089148e62d7519fbaf426626fe3157725b8a");
        ProjectRobot postResult = getPostResult(projectRobotDTO, ROBOT_ADD, status().isOk());
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(postResult.getPlatform(),NoticeConstants.Type.DING_TALK));

    }

    @Test
    @Order(4)
    void addRobotSuccessDingEn() throws Exception {
        setDingEn("钉钉企业应用机器人");
        checkName("test_project", "钉钉企业应用机器人");
    }

    private void setDingEn(String name) throws Exception {
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName(name);
        projectRobotDTO.setPlatform(NoticeConstants.Type.DING_TALK);
        projectRobotDTO.setProjectId("test_project");
        projectRobotDTO.setType(NoticeConstants.DingType.ENTERPRISE);
        projectRobotDTO.setAppKey("dingxwd71o7kj4qoixo7");
        projectRobotDTO.setAppSecret("szmOD9bjGgKtfYk09-Xx2rPdX-xkW4R8Iic0eig_k1D3k95nG4TLKRSpUKUD_f0G");
        projectRobotDTO.setWebhook("https://oapi.dingtalk.com/robot/send?access_token=e971f376669334cd44c585d419f0fdfa1600f97f906109b377999d8a0986b11e");
        ProjectRobot postResult = getPostResult(projectRobotDTO, ROBOT_ADD, status().isOk());
        Assertions.assertTrue(StringUtils.equalsIgnoreCase(postResult.getPlatform(),NoticeConstants.Type.DING_TALK));
    }

    @Test
    @Order(5)
    void addRobotFailDingCustomByType() throws Exception {
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName("钉钉自定义机器人");
        projectRobotDTO.setPlatform(NoticeConstants.Type.DING_TALK);
        projectRobotDTO.setProjectId("test_project");
        projectRobotDTO.setWebhook("https://oapi.dingtalk.com/robot/send?access_token=e971f376669334cd44c585d419f0fdfa1600f97f906109b377999d8a0986b11e");
        getPostResult(projectRobotDTO, ROBOT_ADD, status().is5xxServerError());
    }

    @Test
    @Order(6)
    void addRobotFailDingCustomByKey() throws Exception {
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName("钉钉自定义机器人");
        projectRobotDTO.setPlatform(NoticeConstants.Type.DING_TALK);
        projectRobotDTO.setProjectId("test_project");
        projectRobotDTO.setType(NoticeConstants.DingType.ENTERPRISE);
        projectRobotDTO.setWebhook("https://oapi.dingtalk.com/robot/send?access_token=e971f376669334cd44c585d419f0fdfa1600f97f906109b377999d8a0986b11e");
        getPostResult(projectRobotDTO, ROBOT_ADD, status().is5xxServerError());
    }

    @Test
    @Order(7)
    void addRobotFailDingCustomBySecret() throws Exception {
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName("钉钉自定义机器人");
        projectRobotDTO.setPlatform(NoticeConstants.Type.DING_TALK);
        projectRobotDTO.setProjectId("test_project");
        projectRobotDTO.setType(NoticeConstants.DingType.ENTERPRISE);
        projectRobotDTO.setAppKey("dingxwd71o7kj4qoixo7");
        projectRobotDTO.setWebhook("https://oapi.dingtalk.com/robot/send?access_token=e971f376669334cd44c585d419f0fdfa1600f97f906109b377999d8a0986b11e");
        getPostResult(projectRobotDTO, ROBOT_ADD, status().is5xxServerError());
    }

    @Test
    @Order(8)
    void updateRobotSuccessCusTom() throws Exception {
        setCustomRobot("用于更新自定义机器人","test_project");
        ProjectRobot projectRobot = getRobot("test_project", "用于更新自定义机器人");
        checkUpdate(projectRobot, "更新自定义机器人", status().isOk());
        checkLog(projectRobot.getId(),OperationLogType.UPDATE);
    }

    @Test
    @Order(9)
    void updateRobotSuccessDingCus() throws Exception {
        setDingCustom("test_project","用于更新钉钉自定义机器人");
        ProjectRobot projectRobot = getRobot("test_project", "用于更新钉钉自定义机器人");
        checkUpdate(projectRobot, "更新钉钉自定义机器人", status().isOk());
    }

    @Test
    @Order(10)
    void updateRobotSuccessDingEn() throws Exception {
        setDingEn("用于更新钉钉企业机器人");
        ProjectRobot projectRobot = getRobot("test_project", "用于更新钉钉企业机器人");
        checkUpdate(projectRobot, "更新钉钉企业机器人", status().isOk());
    }

    @Test
    @Order(11)
    void updateRobotFileIdNotExist() throws Exception {
        setCustomRobot("测试没有ID失败","test_project");
        ProjectRobot projectRobot = getRobot("test_project", "测试没有ID失败");
        projectRobot.setId("noId");
        checkUpdate(projectRobot, "测试没有ID失败", status().is5xxServerError());
    }

    @Test
    @Order(12)
    void updateRobotFileIdNoId() throws Exception {
        setCustomRobot("测试ID空失败","test_project");
        ProjectRobot projectRobot = getRobot("test_project", "测试ID空失败");
        projectRobot.setId(null);
        checkUpdate(projectRobot, "测试ID空失败", status().isBadRequest());
    }

    @Test
    @Order(13)
    void updateRobotFileNoDingType() throws Exception {
        setDingCustom("test_project","测试更新没有Type失败");
        ProjectRobot projectRobot = getRobot("test_project", "测试更新没有Type失败");
        projectRobot.setType(null);
        checkUpdate(projectRobot, "测试更新没有Type失败", status().is5xxServerError());
    }

    @Test
    @Order(14)
    void updateRobotFileNoDingKey() throws Exception {
        setDingEn("测试更新没有key失败");
        ProjectRobot projectRobot = getRobot("test_project", "测试更新没有key失败");
        projectRobot.setAppKey(null);
        checkUpdate(projectRobot, "测试更新没有key失败", status().is5xxServerError());
    }

    @Test
    @Order(15)
    void updateRobotFileNoDingSecret() throws Exception {
        setDingEn("测试更新没有Secret失败");
        ProjectRobot projectRobot = getRobot("test_project", "测试更新没有Secret失败");
        projectRobot.setAppSecret(null);
        checkUpdate(projectRobot, "测试更新没有Secret失败", status().is5xxServerError());
    }

    @Test
    @Order(16)
    void deleteRobotSuccess() throws Exception {
        setCustomRobot("测试删除","test_project");
        ProjectRobot projectRobot = getRobot("test_project", "测试删除");
        String projectRobotId = projectRobot.getId();
        mockMvc.perform(MockMvcRequestBuilders.get(ROBOT_DELETE + "/" + projectRobotId)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        checkLog(projectRobotId,OperationLogType.DELETE);
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
    @Order(19)
    void getDetailSuccess() throws Exception {
        setCustomRobot("测试获取详情","test_project");
        ProjectRobot projectRobot = getRobot("test_project", "测试获取详情");
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
    @Order(20)
    void getListSuccessNoKeyword() throws Exception {
        List<ProjectRobot> projectRobots = getList("test_project");
        Assertions.assertTrue(projectRobots.size() > 0);
    }

    @Test
    @Order(21)
    @Sql(scripts = {"/dml/init_project_robot_message.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    void setEnableSuccess() throws Exception {
        setCustomRobot("测试Enable","project-robot-message-test-1");
        ProjectRobot projectRobot = getRobot("project-robot-message-test-1","测试Enable");
        String projectRobotId = projectRobot.getId();
        MessageTaskRequest messageTaskRequest = new MessageTaskRequest();
        messageTaskRequest.setProjectId("project-robot-message-test-1");
        messageTaskRequest.setTaskType(NoticeConstants.TaskType.API_DEFINITION_TASK);
        messageTaskRequest.setEvent(NoticeConstants.Event.CREATE);
        List<String> userIds = new ArrayList<>();
        userIds.add("project-robot-message-user-3");
        userIds.add("project-robot-message-user-4");
        userIds.add("project-robot-message-user-del");
        messageTaskRequest.setReceiverIds(userIds);
        messageTaskRequest.setRobotId(projectRobotId);
        messageTaskRequest.setEnable(true);
        mockMvc.perform(MockMvcRequestBuilders.post("/notice/message/task/save")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(messageTaskRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        mockMvc.perform(MockMvcRequestBuilders.get(ROBOT_ENABLE + "/" + projectRobotId)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        ProjectRobot projectRobotEnable = getRobot("project-robot-message-test-1","测试Enable");
        Assertions.assertFalse(projectRobotEnable.getEnable());
        MvcResult mvcResult1 = mockMvc.perform(MockMvcRequestBuilders.get("/notice/message/task/get/project-robot-message-test-1")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult1.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        List<MessageTaskDTO> messageTaskDetailDTOList = JSON.parseArray(JSON.toJSONString(resultHolder.getData()), MessageTaskDTO.class);
        for (MessageTaskDTO messageTaskDTO : messageTaskDetailDTOList) {
            for (MessageTaskTypeDTO messageTaskTypeDTO : messageTaskDTO.getMessageTaskTypeDTOList()) {
                if (StringUtils.equalsIgnoreCase(messageTaskTypeDTO.getTaskType(),NoticeConstants.TaskType.API_DEFINITION_TASK)) {
                    Boolean testRobotMessageRobot1 = messageTaskTypeDTO.getMessageTaskDetailDTOList().getFirst().getProjectRobotConfigMap().get(projectRobotId).getEnable();
                    Assertions.assertTrue(testRobotMessageRobot1);
                }
            }
        }
    }

    @Test
    @Order(22)
    void setEnableFalseSuccess() throws Exception {
        ProjectRobot projectRobot = getRobot("project-robot-message-test-1","测试Enable");
        String projectRobotId = projectRobot.getId();
        mockMvc.perform(MockMvcRequestBuilders.get(ROBOT_ENABLE + "/" + projectRobotId)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        ProjectRobot projectRobotEnable = getRobot("project-robot-message-test-1","测试Enable");
        Assertions.assertTrue(projectRobotEnable.getEnable());
    }

    @Test
    @Order(23)
    void setEnableFail() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ROBOT_ENABLE + "/no_id")
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }

    @Test
    @Order(24)
    void deleteRobotWithMessage() throws Exception {
        Project project = new Project();
        project.setId("test_project1");
        project.setOrganizationId("organization-message-test");
        project.setName("默认项目");
        project.setDescription("系统默认创建的项目");
        project.setCreateUser("admin");
        project.setUpdateUser("admin");
        project.setCreateTime(System.currentTimeMillis());
        project.setUpdateTime(System.currentTimeMillis());
        projectMapper.insertSelective(project);
        setInSiteCustom("test_project1","站内信1");
        setDingCustom("test_project1","钉钉自定义机器人3");
        ProjectRobot robot = getRobot("test_project1", "钉钉自定义机器人3");
        setMessageTask("test_project1",robot.getId());

        mockMvc.perform(MockMvcRequestBuilders.get(ROBOT_DELETE + "/" + robot.getId())
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @Order(25)
    void getEmptyListSuccess() throws Exception {
        List<ProjectRobot> projectRobots = getList("test_project_x");
        Assertions.assertTrue(CollectionUtils.isEmpty(projectRobots));
    }

    private static ProjectRobot getResult(MvcResult mvcResult) throws UnsupportedEncodingException {
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        return JSON.parseObject(JSON.toJSONString(resultHolder.getData()), ProjectRobot.class);
    }


    private void checkUpdate(ProjectRobot projectRobot, String name, ResultMatcher resultMatcher) throws Exception {
        projectRobot.setName(name);
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        BeanUtils.copyBean(projectRobotDTO, projectRobot);
        getPostResult(projectRobotDTO, ROBOT_UPDATE, resultMatcher);
        if (resultMatcher.equals(status().isOk())) {
            ProjectRobot projectRobotUpdate = getRobot(projectRobot.getProjectId(), name);
            Assertions.assertTrue(StringUtils.equals(projectRobotUpdate.getName(), name));
        }
    }

    private void setCustomRobot(String name, String projectId) throws Exception {
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        projectRobotDTO.setName(name);
        projectRobotDTO.setPlatform(NoticeConstants.Type.CUSTOM);
        projectRobotDTO.setProjectId(projectId);
        projectRobotDTO.setEnable(true);
        projectRobotDTO.setWebhook("https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=2b67ccf4-e0da-4cd6-ae74-8d42657865f8");
        getPostResult(projectRobotDTO, ROBOT_ADD, status().isOk());
    }

    private ProjectRobot getPostResult(ProjectRobotDTO projectRobotDTO, String url, ResultMatcher resultMatcher) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .content(JSON.toJSONString(projectRobotDTO))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(resultMatcher)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        return getResult(mvcResult);
    }

    private List<ProjectRobot> getList(String projectId) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(ROBOT_LIST + projectId)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String sortData = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder sortHolder = JSON.parseObject(sortData, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(sortHolder.getData()), ProjectRobot.class);
    }

    private ProjectRobot getRobot(String projectId, String keyWord) throws Exception {
        ProjectRobotExample projectRobotExample = new ProjectRobotExample();
        projectRobotExample.createCriteria().andProjectIdEqualTo(projectId).andNameEqualTo(keyWord);
        List<ProjectRobot> projectRobots = projectRobotMapper.selectByExample(projectRobotExample);
        return projectRobots.getFirst();
    }

    public void setMessageTask(String projectId, String defaultRobotId) throws Exception {
        StringBuilder jsonStr = new StringBuilder();
        try {
            InputStream inputStream = getClass().getResourceAsStream("/init_message_task.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonStr.append(line);
            }
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        MessageTaskMapper mapper = sqlSession.getMapper(MessageTaskMapper.class);
        MessageTaskBlobMapper blobMapper = sqlSession.getMapper(MessageTaskBlobMapper.class);

        List<MessageTaskDTO> messageTaskDTOList = JSON.parseArray(jsonStr.toString(), MessageTaskDTO.class);
        for (MessageTaskDTO messageTaskDTO : messageTaskDTOList) {
            List<MessageTaskTypeDTO> messageTaskTypeDTOList = messageTaskDTO.getMessageTaskTypeDTOList();
            for (MessageTaskTypeDTO messageTaskTypeDTO : messageTaskTypeDTOList) {
                String taskType = messageTaskTypeDTO.getTaskType();
                if (taskType.contains(NoticeConstants.Mode.SCHEDULE) || taskType.contains("AT")) {
                    continue;
                }
                List<MessageTaskDetailDTO> messageTaskDetailDTOList = messageTaskTypeDTO.getMessageTaskDetailDTOList();
                for (MessageTaskDetailDTO messageTaskDetailDTO : messageTaskDetailDTOList) {
                    String event = messageTaskDetailDTO.getEvent();
                    List<OptionDTO> receivers = messageTaskDetailDTO.getReceivers();
                    if (StringUtils.equalsIgnoreCase(event, NoticeConstants.Event.CREATE) || StringUtils.equalsIgnoreCase(event, NoticeConstants.Event.CASE_CREATE) || StringUtils.equalsIgnoreCase(event, NoticeConstants.Event.MOCK_CREATE) || CollectionUtils.isEmpty(receivers)) {
                        continue;
                    }
                    List<String> receiverIds = receivers.stream().map(OptionDTO::getId).toList();
                    String id = IDGenerator.nextStr();
                    MessageTask messageTask = new MessageTask();
                    messageTask.setId(id);
                    messageTask.setEvent(event);
                    messageTask.setTaskType(taskType);
                    messageTask.setReceivers(receiverIds);
                    messageTask.setProjectId(projectId);
                    messageTask.setProjectRobotId(defaultRobotId);
                    messageTask.setEnable(true);
                    messageTask.setTestId("NONE");
                    messageTask.setCreateUser("admin");
                    messageTask.setCreateTime(System.currentTimeMillis());
                    messageTask.setUpdateUser("admin");
                    messageTask.setUpdateTime(System.currentTimeMillis());
                    messageTask.setSubject("");
                    messageTask.setUseDefaultSubject(true);
                    messageTask.setUseDefaultTemplate(true);
                    MessageTaskBlob messageTaskBlob = new MessageTaskBlob();
                    messageTaskBlob.setId(id);
                    messageTaskBlob.setTemplate("");
                    mapper.insert(messageTask);
                    blobMapper.insert(messageTaskBlob);
                }
            }
        }

        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        List<MessageTaskDTO> testProject1 = getMessageList(projectId);
        Assertions.assertTrue(testProject1.size()>0);
    }

    private List<MessageTaskDTO> getMessageList(String projectId) throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/notice/message/task/get/"+projectId)
                        .header(SessionConstants.HEADER_TOKEN, sessionId)
                        .header(SessionConstants.CSRF_TOKEN, csrfToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        ResultHolder resultHolder = JSON.parseObject(contentAsString, ResultHolder.class);
        return JSON.parseArray(JSON.toJSONString(resultHolder.getData()), MessageTaskDTO.class);
    }

    protected void checkLog(String resourceId, OperationLogType operationLogType) throws Exception {
        OperationLogExample example = new OperationLogExample();
        example.createCriteria().andSourceIdEqualTo(resourceId).andTypeEqualTo(operationLogType.name());
        operationLogMapper.selectByExample(example).stream()
                .filter(operationLog -> operationLog.getSourceId().equalsIgnoreCase(resourceId))
                .filter(operationLog -> operationLog.getType().equalsIgnoreCase(operationLogType.name()))
                .filter(operationLog -> StringUtils.isNotBlank(operationLog.getProjectId()))
                .filter(operationLog -> StringUtils.isNotBlank(operationLog.getModule()))
                .findFirst()
                .orElseThrow(() -> new Exception("日志不存在，请补充操作日志"));
    }

    protected void checkContentLog(String content, OperationLogType operationLogType) throws Exception {
        OperationLogExample example = new OperationLogExample();
        example.createCriteria().andContentEqualTo(content).andTypeEqualTo(operationLogType.name());
        operationLogMapper.selectByExample(example).stream()
                .filter(operationLog -> operationLog.getContent().equalsIgnoreCase(content))
                .filter(operationLog -> operationLog.getType().equalsIgnoreCase(operationLogType.name()))
                .filter(operationLog -> StringUtils.isNotBlank(operationLog.getProjectId()))
                .filter(operationLog -> StringUtils.isNotBlank(operationLog.getModule()))
                .findFirst()
                .orElseThrow(() -> new Exception("日志不存在，请补充操作日志"));
    }
}
