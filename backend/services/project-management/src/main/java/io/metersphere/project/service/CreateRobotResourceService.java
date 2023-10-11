package io.metersphere.project.service;

import io.metersphere.project.domain.MessageTask;
import io.metersphere.project.domain.MessageTaskBlob;
import io.metersphere.project.domain.ProjectRobot;
import io.metersphere.project.dto.MessageTaskDTO;
import io.metersphere.project.dto.MessageTaskDetailDTO;
import io.metersphere.project.dto.MessageTaskTypeDTO;
import io.metersphere.project.enums.ProjectRobotPlatform;
import io.metersphere.project.mapper.MessageTaskBlobMapper;
import io.metersphere.project.mapper.MessageTaskMapper;
import io.metersphere.project.mapper.ProjectRobotMapper;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.service.CreateProjectResourceService;
import io.metersphere.system.uid.UUID;
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
import java.util.List;

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
        String inSiteId = UUID.randomUUID().toString();
        projectRobot.setId(inSiteId);
        projectRobot.setProjectId(projectId);
        projectRobot.setName(Translator.get("robot_in_site"));
        projectRobot.setPlatform(ProjectRobotPlatform.IN_SITE.toString());
        projectRobot.setWebhook("NONE");
        projectRobot.setCreateUser("admin");
        projectRobot.setCreateTime(System.currentTimeMillis());
        projectRobot.setUpdateUser("admin");
        projectRobot.setUpdateTime(System.currentTimeMillis());
        projectRobot.setEnable(true);
        projectRobot.setDescription(Translator.get("robot_in_site_description"));
        list.add(projectRobot);
        ProjectRobot projectRobotMail = new ProjectRobot();
        projectRobotMail.setId(UUID.randomUUID().toString());
        projectRobotMail.setProjectId(projectId);
        projectRobotMail.setName(Translator.get("robot_mail"));
        projectRobotMail.setPlatform(ProjectRobotPlatform.MAIL.toString());
        projectRobotMail.setWebhook("NONE");
        projectRobotMail.setCreateUser("admin");
        projectRobotMail.setEnable(true);
        projectRobotMail.setDescription(Translator.get("robot_mail_description"));
        projectRobotMail.setCreateTime(System.currentTimeMillis());
        projectRobotMail.setUpdateUser("admin");
        projectRobotMail.setUpdateTime(System.currentTimeMillis());
        list.add(projectRobotMail);
        robotMapper.batchInsert(list);
        setMessageTask(projectId, inSiteId);
    }

    public void setMessageTask(String projectId, String defaultRobotId) {
        StringBuilder jsonStr = new StringBuilder();
        InputStream inputStream = getClass().getResourceAsStream("/message_task.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                jsonStr.append(line);
            }
            reader.close();
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
                if (taskType.contains(NoticeConstants.Mode.SCHEDULE) || taskType.contains("AT") || taskType.contains("JENKINS")) {
                    continue;
                }
                List<MessageTaskDetailDTO> messageTaskDetailDTOList = messageTaskTypeDTO.getMessageTaskDetailDTOList();
                for (MessageTaskDetailDTO messageTaskDetailDTO : messageTaskDetailDTOList) {
                    String event = messageTaskDetailDTO.getEvent();
                    List<OptionDTO> receivers = messageTaskDetailDTO.getReceivers();
                    if (StringUtils.equalsIgnoreCase(event, NoticeConstants.Event.CREATE) || StringUtils.equalsIgnoreCase(event, NoticeConstants.Event.CASE_CREATE) || CollectionUtils.isEmpty(receivers)) {
                        continue;
                    }
                    for (OptionDTO receiver : receivers) {
                        String id = UUID.randomUUID().toString();
                        MessageTask messageTask = new MessageTask();
                        messageTask.setId(id);
                        messageTask.setEvent(event);
                        messageTask.setTaskType(taskType);
                        messageTask.setReceiver(receiver.getId());
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

        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
    }
}
