package io.metersphere.project.service;

import io.metersphere.project.domain.MessageTask;
import io.metersphere.project.domain.MessageTaskBlob;
import io.metersphere.project.domain.ProjectRobot;
import io.metersphere.project.dto.MessageTaskDTO;
import io.metersphere.project.dto.MessageTaskDetailDTO;
import io.metersphere.project.dto.MessageTaskTypeDTO;
import io.metersphere.project.mapper.MessageTaskBlobMapper;
import io.metersphere.project.mapper.MessageTaskMapper;
import io.metersphere.project.mapper.ProjectRobotMapper;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.CreateProjectResourceService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CreateRobotResourceService implements CreateProjectResourceService {

    @Resource
    private ProjectRobotMapper robotMapper;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void createResources(String projectId) {
        List<ProjectRobot> list = new ArrayList<>();
        ProjectRobot projectRobot = new ProjectRobot();
        String inSiteId = IDGenerator.nextStr();
        projectRobot.setId(inSiteId);
        projectRobot.setProjectId(projectId);
        projectRobot.setName("robot_in_site");
        projectRobot.setPlatform(NoticeConstants.Type.IN_SITE);
        projectRobot.setWebhook("NONE");
        projectRobot.setCreateUser("admin");
        projectRobot.setCreateTime(System.currentTimeMillis());
        projectRobot.setUpdateUser("admin");
        projectRobot.setUpdateTime(System.currentTimeMillis());
        projectRobot.setEnable(true);
        projectRobot.setDescription("robot_in_site_description");
        list.add(projectRobot);
        ProjectRobot projectRobotMail = new ProjectRobot();
        projectRobotMail.setId(IDGenerator.nextStr());
        projectRobotMail.setProjectId(projectId);
        projectRobotMail.setName("robot_mail");
        projectRobotMail.setPlatform(NoticeConstants.Type.MAIL);
        projectRobotMail.setWebhook("NONE");
        projectRobotMail.setCreateUser("admin");
        projectRobotMail.setEnable(false);
        projectRobotMail.setDescription("robot_mail_description");
        projectRobotMail.setCreateTime(System.currentTimeMillis());
        projectRobotMail.setUpdateUser("admin");
        projectRobotMail.setUpdateTime(System.currentTimeMillis());
        list.add(projectRobotMail);
        robotMapper.batchInsert(list);
        setMessageTask(projectId, inSiteId);
    }

    public void setMessageTask(String projectId, String defaultRobotId) {
        StringBuilder jsonStr = new StringBuilder();
        try {
            InputStream inputStream = getClass().getResourceAsStream("/init_message_task.json");
            assert inputStream != null;
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
        //生成消息管理默认显示数据
        setTemplateMessageTask(projectId, defaultRobotId, jsonStr, mapper, blobMapper);
        //生成 内置at 的消息管理数据
        setAtMessageTask(projectId, defaultRobotId, mapper, blobMapper);

        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }

    private static void setTemplateMessageTask(String projectId, String defaultRobotId, StringBuilder jsonStr, MessageTaskMapper mapper, MessageTaskBlobMapper blobMapper) {
        List<MessageTaskDTO> messageTaskDTOList = JSON.parseArray(jsonStr.toString(), MessageTaskDTO.class);
        for (MessageTaskDTO messageTaskDTO : messageTaskDTOList) {
            List<MessageTaskTypeDTO> messageTaskTypeDTOList = messageTaskDTO.getMessageTaskTypeDTOList();
            for (MessageTaskTypeDTO messageTaskTypeDTO : messageTaskTypeDTOList) {
                String taskType = messageTaskTypeDTO.getTaskType();
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
    }

    private static void setAtMessageTask(String projectId, String defaultRobotId, MessageTaskMapper mapper, MessageTaskBlobMapper blobMapper) {
        Map<String,List<String>> taskTypeEventMap = new HashMap<>();
        List<String>bugEventList = new ArrayList<>();
        bugEventList.add(NoticeConstants.Event.AT);
        bugEventList.add(NoticeConstants.Event.REPLY);
        taskTypeEventMap.put(NoticeConstants.TaskType.BUG_TASK,bugEventList);
        List<String>funcationalCaseEventList = new ArrayList<>();
        funcationalCaseEventList.add(NoticeConstants.Event.AT);
        funcationalCaseEventList.add(NoticeConstants.Event.REPLY);
        funcationalCaseEventList.add(NoticeConstants.Event.REVIEW_AT);
        funcationalCaseEventList.add(NoticeConstants.Event.EXECUTE_AT);
        taskTypeEventMap.put(NoticeConstants.TaskType.FUNCTIONAL_CASE_TASK,funcationalCaseEventList);
        taskTypeEventMap.forEach((taskType, eventList)->{
            for (String event : eventList) {
                String id = IDGenerator.nextStr();
                MessageTask messageTask = new MessageTask();
                messageTask.setId(id);
                messageTask.setEvent(event);
                messageTask.setTaskType(taskType);
                messageTask.setReceivers(new ArrayList<>());
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
        });
    }
}
