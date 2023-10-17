package io.metersphere.project.service;


import io.metersphere.project.domain.*;
import io.metersphere.project.dto.*;
import io.metersphere.project.enums.ProjectRobotPlatform;
import io.metersphere.project.enums.result.ProjectResultCode;
import io.metersphere.project.mapper.*;
import io.metersphere.sdk.dto.OptionDTO;
import io.metersphere.sdk.dto.request.MessageTaskRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.uid.UUID;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class NoticeMessageTaskService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;
    @Resource
    private ProjectRobotMapper projectRobotMapper;
    @Resource
    private MessageTaskMapper messageTaskMapper;
    @Resource
    private MessageTaskBlobMapper messageTaskBlobMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private ExtProjectUserRoleMapper extProjectUserRoleMapper;


    public static final String USER_IDS = "user_ids";

    public static final String NO_USER_NAMES = "no_user_names";


    public ResultHolder saveMessageTask(MessageTaskRequest messageTaskRequest, String userId) {
        String projectId = messageTaskRequest.getProjectId();
        checkProjectExist(projectId);
        //如果只选了用户，没有选机器人，默认机器人为站内信
        ProjectRobot projectRobot = getDefaultRobot(messageTaskRequest.getProjectId(), messageTaskRequest.getRobotId());
        String robotId = projectRobot.getId();
        messageTaskRequest.setRobotId(robotId);
        //删除用户数据不在当前传送用户内的数据
        deleteUserData(messageTaskRequest, projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        MessageTaskMapper mapper = sqlSession.getMapper(MessageTaskMapper.class);
        MessageTaskBlobMapper blobMapper = sqlSession.getMapper(MessageTaskBlobMapper.class);
        //检查用户是否存在
        Map<String, List<String>> stringListMap = checkUserExistProject(messageTaskRequest.getReceiverIds(), projectId);
        List<String> existUserIds = stringListMap.get(USER_IDS);
        //检查设置的通知是否存在，如果存在则更新
        List<MessageTask> messageTasks = updateMessageTasks(messageTaskRequest, userId, mapper, blobMapper, existUserIds);
        //保存消息任务
        List<String> messageTaskReceivers = CollectionUtils.isEmpty(messageTasks) ? new ArrayList<>() : messageTasks.stream().map(MessageTask::getReceiver).toList();
        insertMessageTask(messageTaskRequest, userId, mapper, blobMapper, existUserIds, messageTaskReceivers);
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        if (CollectionUtils.isNotEmpty(stringListMap.get(NO_USER_NAMES))) {
            String message = Translator.get("alert_others") + stringListMap.get(NO_USER_NAMES).get(0) + Translator.get("user.remove");
            return ResultHolder.successCodeErrorInfo(ProjectResultCode.SAVE_MESSAGE_TASK_USER_NO_EXIST.getCode(), message);
        }
        return ResultHolder.success("OK");
    }

    private void deleteUserData(MessageTaskRequest messageTaskRequest, String projectId) {
        MessageTaskExample messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andReceiverNotIn(messageTaskRequest.getReceiverIds())
                .andProjectIdEqualTo(projectId).andProjectRobotIdEqualTo(messageTaskRequest.getRobotId()).andTaskTypeEqualTo(messageTaskRequest.getTaskType()).andEventEqualTo(messageTaskRequest.getEvent());
        List<MessageTask> delData = messageTaskMapper.selectByExample(messageTaskExample);
        List<String> delIds = delData.stream().map(MessageTask::getId).toList();
        if (CollectionUtils.isNotEmpty(delIds)) {
            MessageTaskBlobExample messageTaskBlobExample = new MessageTaskBlobExample();
            messageTaskBlobExample.createCriteria().andIdIn(delIds);
            messageTaskBlobMapper.deleteByExample(messageTaskBlobExample);
            messageTaskMapper.deleteByExample(messageTaskExample);
        }
    }

    private void checkProjectExist(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            throw new MSException(Translator.get("project_is_not_exist"));
        }
    }

    /**
     * 新增MessageTask
     *
     * @param messageTaskRequest   入参
     * @param userId               当前用户i的
     * @param mapper               MessageTaskMapper
     * @param blobMapper           MessageTaskBlobMapper
     * @param existUserIds         系统中还存在的入参传过来的接收人
     * @param messageTaskReceivers 更新过后还有多少接收人需要保存
     */
    private void insertMessageTask(MessageTaskRequest messageTaskRequest, String userId, MessageTaskMapper mapper, MessageTaskBlobMapper blobMapper, List<String> existUserIds, List<String> messageTaskReceivers) {
        List<MessageTask> messageTasks = new ArrayList<>();
        for (String receiverId : existUserIds) {
            if (CollectionUtils.isNotEmpty(messageTaskReceivers) && messageTaskReceivers.contains(receiverId)) {
                continue;
            }
            MessageTask messageTask = new MessageTask();
            buildMessageTask(messageTaskRequest, userId, receiverId, messageTask);
            mapper.insert(messageTask);
            MessageTaskBlob messageTaskBlob = new MessageTaskBlob();
            messageTaskBlob.setId(messageTask.getId());
            if (!messageTask.getUseDefaultTemplate()) {
                messageTaskBlob.setTemplate(messageTaskRequest.getTemplate());
            }
            messageTaskBlob.setTemplate(messageTaskRequest.getTemplate());
            messageTasks.add(messageTask);
            blobMapper.insert(messageTaskBlob);
        }
    }

    /**
     * 查询默认机器人id
     *
     * @param projectId 项目id
     * @param robotId   机器人id
     * @return String
     */
    private ProjectRobot getDefaultRobot(String projectId, String robotId) {
        if (StringUtils.isBlank(robotId)) {
            ProjectRobotExample projectRobotExample = new ProjectRobotExample();
            projectRobotExample.createCriteria().andProjectIdEqualTo(projectId).andPlatformEqualTo(ProjectRobotPlatform.IN_SITE.toString());
            List<ProjectRobot> projectRobots = projectRobotMapper.selectByExample(projectRobotExample);
            return projectRobots.get(0);
        } else {
            return projectRobotMapper.selectByPrimaryKey(robotId);
        }
    }

    /**
     * 检查数据库是否有同类型数据，有则更新
     *
     * @param messageTaskRequest 入参
     * @param userId             当前用户ID
     * @param mapper             MessageTaskMapper
     * @param blobMapper         MessageTaskBlobMapper
     * @param existUserIds       系统中还存在的入参传过来的接收人
     * @return List<MessageTask>
     */
    private List<MessageTask> updateMessageTasks(MessageTaskRequest messageTaskRequest, String userId, MessageTaskMapper mapper, MessageTaskBlobMapper blobMapper, List<String> existUserIds) {
        boolean enable = messageTaskRequest.getEnable() != null && messageTaskRequest.getEnable();
        boolean useDefaultSubject = messageTaskRequest.getUseDefaultSubject() == null || messageTaskRequest.getUseDefaultSubject();
        boolean useDefaultTemplate = messageTaskRequest.getUseDefaultTemplate() == null || messageTaskRequest.getUseDefaultTemplate();

        MessageTaskExample messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andReceiverIn(existUserIds).andProjectIdEqualTo(messageTaskRequest.getProjectId())
                .andProjectRobotIdEqualTo(messageTaskRequest.getRobotId()).andTaskTypeEqualTo(messageTaskRequest.getTaskType()).andEventEqualTo(messageTaskRequest.getEvent());
        List<MessageTask> messageTasks = messageTaskMapper.selectByExample(messageTaskExample);
        List<String> messageTaskIds = messageTasks.stream().map(MessageTask::getId).toList();
        if (CollectionUtils.isEmpty(messageTasks)) {
            return new ArrayList<>();
        }
        for (MessageTask messageTask : messageTasks) {
            messageTask.setUpdateTime(System.currentTimeMillis());
            messageTask.setUpdateUser(userId);
            messageTask.setEnable(enable);
            messageTask.setUseDefaultSubject(useDefaultSubject);
            messageTask.setUseDefaultTemplate(useDefaultTemplate);
            if (!useDefaultSubject) {
                messageTask.setSubject(messageTaskRequest.getSubject());
            }
            mapper.updateByPrimaryKeySelective(messageTask);
        }
        MessageTaskBlobExample messageTaskBlobExample = new MessageTaskBlobExample();
        messageTaskBlobExample.createCriteria().andIdIn(messageTaskIds);
        List<MessageTaskBlob> messageTaskBlobs = messageTaskBlobMapper.selectByExample(messageTaskBlobExample);
        for (MessageTaskBlob messageTaskBlob : messageTaskBlobs) {
            if (!useDefaultTemplate) {
                messageTaskBlob.setTemplate(messageTaskRequest.getTemplate());
                blobMapper.updateByPrimaryKeySelective(messageTaskBlob);
            }
        }
        return messageTasks;
    }

    private static void buildMessageTask(MessageTaskRequest messageTaskRequest, String userId, String receiverId, MessageTask messageTask) {
        String testId = messageTaskRequest.getTestId() == null ? "NONE" : messageTaskRequest.getTestId();
        boolean enable = messageTaskRequest.getEnable() != null && messageTaskRequest.getEnable();
        boolean useDefaultSubject = messageTaskRequest.getUseDefaultSubject() == null || messageTaskRequest.getUseDefaultSubject();
        boolean useDefaultTemplate = messageTaskRequest.getUseDefaultTemplate() == null || messageTaskRequest.getUseDefaultTemplate();

        String insertId = UUID.randomUUID().toString();
        messageTask.setId(insertId);
        messageTask.setTaskType(messageTaskRequest.getTaskType());
        messageTask.setEvent(messageTaskRequest.getEvent());
        messageTask.setReceiver(receiverId);
        messageTask.setProjectId(messageTaskRequest.getProjectId());
        messageTask.setProjectRobotId(messageTaskRequest.getRobotId());
        messageTask.setTestId(testId);
        messageTask.setCreateUser(userId);
        messageTask.setCreateTime(System.currentTimeMillis());
        messageTask.setUpdateUser(userId);
        messageTask.setUpdateTime(System.currentTimeMillis());
        messageTask.setEnable(enable);
        messageTask.setUseDefaultTemplate(useDefaultTemplate);
        messageTask.setUseDefaultSubject(useDefaultSubject);
        if (!useDefaultSubject) {
            messageTask.setSubject(messageTaskRequest.getSubject());
        }
    }

    /**
     * 检查用户是否存在
     *
     * @param receiverIds 接收人ids
     * @param projectId   项目id
     * @return Map<String, List < String>>
     */
    private Map<String, List<String>> checkUserExistProject(List<String> receiverIds, String projectId) {
        List<String> normalUserIds = new ArrayList<>();
        List<String> defaultRelatedUser = MessageTemplateUtils.getDefaultRelatedUser();
        for (String receiverId : receiverIds) {
            if (!defaultRelatedUser.contains(receiverId)) {
                normalUserIds.add(receiverId);
            }
        }
        List<String> userIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(normalUserIds)) {
            UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
            userRoleRelationExample.createCriteria().andUserIdIn(normalUserIds).andSourceIdEqualTo(projectId);
            List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
            userIds = new ArrayList<>(userRoleRelations.stream().map(UserRoleRelation::getUserId).distinct().toList());
        }
        if (receiverIds.contains(NoticeConstants.RelatedUser.CREATE_USER)) {
            userIds.add(NoticeConstants.RelatedUser.CREATE_USER);
        }
        if (receiverIds.contains(NoticeConstants.RelatedUser.FOLLOW_PEOPLE)) {
            userIds.add(NoticeConstants.RelatedUser.FOLLOW_PEOPLE);
        }
        if (receiverIds.contains(NoticeConstants.RelatedUser.OPERATOR)) {
            userIds.add(NoticeConstants.RelatedUser.OPERATOR);
        }
        Map<String, List<String>> map = new HashMap<>();
        List<String> noUserNames = new ArrayList<>();
        if (userIds.size() < receiverIds.size()) {
            for (String receiverId : receiverIds) {
                if (!defaultRelatedUser.contains(receiverId) && !userIds.contains(receiverId)) {
                    User user = userMapper.selectByPrimaryKey(receiverId);
                    if (user == null) {
                        noUserNames.add(receiverId);
                    } else {
                        noUserNames.add(user.getName());
                    }
                    break;
                }
            }
        }
        map.put(NO_USER_NAMES, noUserNames);
        map.put(USER_IDS, userIds);
        return map;
    }

    /**
     * 根据项目id 获取当前项目的消息设置
     *
     * @param projectId 项目ID
     * @return List<MessageTaskDTO>
     */
    public List<MessageTaskDTO> getMessageList(String projectId) throws IOException {
        checkProjectExist(projectId);
        //获取返回数据结构
        StringBuilder jsonStr = new StringBuilder();
        InputStream inputStream = getClass().getResourceAsStream("/message_task.json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            jsonStr.append(line);
        }
        reader.close();
        List<MessageTaskDTO> messageTaskDTOList = JSON.parseArray(jsonStr.toString(), MessageTaskDTO.class);
        //查询数据
        MessageTaskExample messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andProjectIdEqualTo(projectId);
        List<MessageTask> messageTasks = messageTaskMapper.selectByExample(messageTaskExample);
        if (CollectionUtils.isEmpty(messageTasks)) {
            return messageTaskDTOList;
        }
        List<String> messageTaskIds = messageTasks.stream().map(MessageTask::getId).toList();
        MessageTaskBlobExample messageTaskBlobExample = new MessageTaskBlobExample();
        messageTaskBlobExample.createCriteria().andIdIn(messageTaskIds);
        List<MessageTaskBlob> messageTaskBlobs = messageTaskBlobMapper.selectByExampleWithBLOBs(messageTaskBlobExample);
        List<String> robotIds = messageTasks.stream().map(MessageTask::getProjectRobotId).toList();
        ProjectRobotExample projectRobotExample = new ProjectRobotExample();
        projectRobotExample.createCriteria().andIdIn(robotIds);
        List<ProjectRobot> projectRobots = projectRobotMapper.selectByExample(projectRobotExample);
        Map<String, ProjectRobot> robotMap = projectRobots.stream().collect(Collectors.toMap(ProjectRobot::getId, item -> item));
        List<String> userIds = messageTasks.stream().map(MessageTask::getReceiver).toList();
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIds).andDeletedEqualTo(false);
        List<User> users = userMapper.selectByExample(userExample);
        Map<String, String> userNameMap = users.stream().collect(Collectors.toMap(User::getId, User::getName));
        Map<String, String> defaultRelatedUserMap = MessageTemplateUtils.getDefaultRelatedUserMap();
        userNameMap.putAll(defaultRelatedUserMap);
        //开始准备数据
        Map<String, MessageTaskBlob> messageTaskBlobMap = messageTaskBlobs.stream().collect(Collectors.toMap(MessageTaskBlob::getId, item -> item));
        Map<String, List<MessageTask>> messageMap = messageTasks.stream().collect(Collectors.groupingBy(MessageTask::getTaskType));
        //获取默认数据
        Map<String, String> moduleMap = MessageTemplateUtils.getModuleMap();
        Map<String, String> taskTypeMap = MessageTemplateUtils.getTaskTypeMap();
        Map<String, String> eventMap = MessageTemplateUtils.getEventMap();
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        Map<String, String> defaultTemplateSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        ProjectRobot projectRobot = getDefaultRobot(projectId, null);
        for (MessageTaskDTO messageTaskDTO : messageTaskDTOList) {
            messageTaskDTO.setProjectId(projectId);
            messageTaskDTO.setName(moduleMap.get(messageTaskDTO.getType()));
            for (MessageTaskTypeDTO messageTaskTypeDTO : messageTaskDTO.getMessageTaskTypeDTOList()) {
                List<MessageTask> messageTaskTypeList = messageMap.get(messageTaskTypeDTO.getTaskType());
                if (CollectionUtils.isEmpty(messageTaskTypeList)) {
                    continue;
                }
                Map<String, List<MessageTask>> messageEventMap = messageTaskTypeList.stream().collect(Collectors.groupingBy(MessageTask::getEvent));
                messageTaskTypeDTO.setTaskTypeName(taskTypeMap.get(messageTaskTypeDTO.getTaskType()));
                for (MessageTaskDetailDTO messageTaskDetailDTO : messageTaskTypeDTO.getMessageTaskDetailDTOList()) {
                    messageTaskDetailDTO.setEventName(eventMap.get(messageTaskDetailDTO.getEvent()));
                    List<MessageTask> messageTaskList = messageEventMap.get(messageTaskDetailDTO.getEvent());
                    List<OptionDTO> receivers = new ArrayList<>();
                    Map<String, ProjectRobotConfigDTO> projectRobotConfigMap = new HashMap<>();
                    String defaultTemplate = defaultTemplateMap.get(messageTaskTypeDTO.getTaskType() + "_" + messageTaskDetailDTO.getEvent());
                    if (CollectionUtils.isEmpty(messageTaskList)) {
                        String defaultSubject = defaultTemplateSubjectMap.get(messageTaskTypeDTO.getTaskType() + "_" + messageTaskDetailDTO.getEvent());
                        ProjectRobotConfigDTO projectRobotConfigDTO = getDefaultProjectRobotConfigDTO(messageTaskTypeDTO.getTaskType(), defaultTemplate, defaultSubject, projectRobot);
                        projectRobotConfigMap.put(projectRobot.getId(), projectRobotConfigDTO);
                    } else {
                        for (MessageTask messageTask : messageTaskList) {
                            MessageTaskBlob messageTaskBlob = messageTaskBlobMap.get(messageTask.getId());
                            OptionDTO optionDTO = new OptionDTO();
                            optionDTO.setId(messageTask.getReceiver());
                            optionDTO.setName(userNameMap.get(messageTask.getReceiver()));
                            receivers.add(optionDTO);
                            String platform = robotMap.get(messageTask.getProjectRobotId()).getPlatform();
                            String defaultSubject;
                            if (StringUtils.equalsIgnoreCase(platform, ProjectRobotPlatform.MAIL.toString())) {
                                defaultSubject = "MeterSphere " + defaultTemplateSubjectMap.get(messageTaskTypeDTO.getTaskType() + "_" + messageTaskDetailDTO.getEvent());
                            } else {
                                defaultSubject = defaultTemplateSubjectMap.get(messageTaskTypeDTO.getTaskType() + "_" + messageTaskDetailDTO.getEvent());
                            }
                            ProjectRobotConfigDTO projectRobotConfigDTO = getProjectRobotConfigDTO(defaultTemplate, defaultSubject, robotMap.get(messageTask.getProjectRobotId()), messageTask, messageTaskBlob);
                            projectRobotConfigMap.put(messageTask.getProjectRobotId(), projectRobotConfigDTO);
                        }
                    }
                    List<OptionDTO> distinctReceivers = receivers.stream().distinct().toList();
                    messageTaskDetailDTO.setReceivers(distinctReceivers);
                    messageTaskDetailDTO.setProjectRobotConfigMap(projectRobotConfigMap);
                }
            }
        }
        return messageTaskDTOList;
    }

    private ProjectRobotConfigDTO getProjectRobotConfigDTO(String defaultTemplate, String defaultSubject, ProjectRobot projectRobot, MessageTask messageTask, MessageTaskBlob messageTaskBlob) {
        ProjectRobotConfigDTO projectRobotConfigDTO = new ProjectRobotConfigDTO();
        if (StringUtils.equalsIgnoreCase(projectRobot.getName(),"robot_in_site") || StringUtils.equalsIgnoreCase(projectRobot.getName(),"robot_mail")) {
            projectRobotConfigDTO.setRobotName(Translator.get(projectRobot.getName()));
        } else {
            projectRobotConfigDTO.setRobotName(projectRobot.getName());
        }
        projectRobotConfigDTO.setRobotId(projectRobot.getId());
        projectRobotConfigDTO.setPlatform(projectRobot.getPlatform());
        projectRobotConfigDTO.setDingType(projectRobot.getType());
        projectRobotConfigDTO.setEnable(messageTask.getEnable());
        if (messageTask.getUseDefaultSubject()) {
            projectRobotConfigDTO.setSubject(defaultSubject);
        } else {
            projectRobotConfigDTO.setSubject(messageTask.getSubject());
        }
        if (messageTask.getUseDefaultTemplate()) {
            projectRobotConfigDTO.setTemplate(defaultTemplate);
        } else {
            projectRobotConfigDTO.setTemplate(messageTaskBlob.getTemplate());
        }
        String translateTemplate = MessageTemplateUtils.getTranslateTemplate(messageTask.getTaskType(), projectRobotConfigDTO.getTemplate());
        String translateSubject = MessageTemplateUtils.getTranslateSubject(messageTask.getTaskType(), projectRobotConfigDTO.getSubject());
        projectRobotConfigDTO.setPreviewTemplate(translateTemplate);
        projectRobotConfigDTO.setPreviewSubject(translateSubject);
        projectRobotConfigDTO.setDefaultTemplate(defaultTemplate);
        projectRobotConfigDTO.setDefaultSubject(defaultSubject);
        projectRobotConfigDTO.setUseDefaultSubject(messageTask.getUseDefaultSubject());
        projectRobotConfigDTO.setUseDefaultTemplate(messageTask.getUseDefaultTemplate());
        return projectRobotConfigDTO;
    }

    private static ProjectRobotConfigDTO getDefaultProjectRobotConfigDTO(String taskType, String defaultTemplate, String defaultSubject, ProjectRobot projectRobot) {
        ProjectRobotConfigDTO projectRobotConfigDTO = new ProjectRobotConfigDTO();
        projectRobotConfigDTO.setRobotId(projectRobot.getId());
        projectRobotConfigDTO.setRobotName(Translator.get(projectRobot.getName()));
        projectRobotConfigDTO.setPlatform(ProjectRobotPlatform.IN_SITE.toString());
        projectRobotConfigDTO.setDingType(projectRobot.getType());
        projectRobotConfigDTO.setEnable(false);
        projectRobotConfigDTO.setTemplate(defaultTemplate);
        projectRobotConfigDTO.setDefaultTemplate(defaultTemplate);
        projectRobotConfigDTO.setSubject(defaultSubject);
        projectRobotConfigDTO.setDefaultSubject(defaultSubject);
        projectRobotConfigDTO.setUseDefaultSubject(true);
        projectRobotConfigDTO.setUseDefaultTemplate(true);
        String translateTemplate = MessageTemplateUtils.getTranslateTemplate(taskType, defaultTemplate);
        String translateSubject = MessageTemplateUtils.getTranslateSubject(taskType, defaultSubject);
        projectRobotConfigDTO.setPreviewTemplate(translateTemplate);
        projectRobotConfigDTO.setPreviewSubject(translateSubject);
        return projectRobotConfigDTO;
    }

    public List<OptionDTO> getUserList(String projectId, String keyword) {
        List<OptionDTO> projectUserSelectList = extProjectUserRoleMapper.getProjectUserSelectList(projectId, keyword);
        Map<String, String> defaultRelatedUserMap = MessageTemplateUtils.getDefaultRelatedUserMap();
        defaultRelatedUserMap.forEach((k, v) -> {
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(k);
            optionDTO.setName(v);
            projectUserSelectList.add(optionDTO);
        });
        return projectUserSelectList;
    }

    public MessageTemplateConfigDTO getTemplateDetail(String projectId, String taskType, String event, String robotId) {
        MessageTaskExample messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andProjectIdEqualTo(projectId).andTaskTypeEqualTo(taskType).andEventEqualTo(event);
        List<MessageTask> messageTasks = messageTaskMapper.selectByExample(messageTaskExample);
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        Map<String, String> defaultTemplateSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        ProjectRobot projectRobot = projectRobotMapper.selectByPrimaryKey(robotId);
        MessageTask messageTask;
        if (projectRobot == null) {
            throw new MSException(Translator.get("robot_is_null"));
        }
        if (CollectionUtils.isEmpty(messageTasks)) {
            messageTask = new MessageTask();
            messageTask.setTaskType(taskType);
            messageTask.setEvent(event);
            messageTask.setEnable(false);
            messageTask.setUseDefaultTemplate(true);
            messageTask.setUseDefaultSubject(true);
            messageTask.setProjectRobotId(projectRobot.getId());
            messageTask.setProjectId(projectId);
            messageTasks.add(messageTask);
        }
        List<String> receiverIds = messageTasks.stream().map(MessageTask::getReceiver).filter(Objects::nonNull).distinct().toList();
        Map<String, List<MessageTask>> messageRobotMap = messageTasks.stream().collect(Collectors.groupingBy(MessageTask::getProjectRobotId));
        if (CollectionUtils.isNotEmpty(messageRobotMap.get(robotId))) {
            messageTask = messageRobotMap.get(robotId).get(0);
        } else {
            messageTask = messageTasks.get(0);
            messageTask.setEnable(false);
            messageTask.setUseDefaultTemplate(true);
            messageTask.setUseDefaultSubject(true);
        }
        MessageTaskBlob messageTaskBlob = messageTaskBlobMapper.selectByPrimaryKey(messageTask.getId());
        String defaultTemplate = defaultTemplateMap.get(messageTask.getTaskType() + "_" + messageTask.getEvent());
        String defaultSubject = defaultTemplateSubjectMap.get(messageTask.getTaskType() + "_" + messageTask.getEvent());
        ProjectRobotConfigDTO projectRobotConfigDTO = getProjectRobotConfigDTO(defaultTemplate, defaultSubject, projectRobot, messageTask, messageTaskBlob);
        MessageTemplateConfigDTO messageTemplateConfigDTO = new MessageTemplateConfigDTO();
        BeanUtils.copyBean(messageTemplateConfigDTO, projectRobotConfigDTO);
        Map<String, String> taskTypeMap = MessageTemplateUtils.getTaskTypeMap();
        Map<String, String> eventMap = MessageTemplateUtils.getEventMap();
        messageTemplateConfigDTO.setTaskTypeName(taskTypeMap.get(messageTask.getTaskType()));
        messageTemplateConfigDTO.setEventName(eventMap.get(messageTask.getEvent()));
        messageTemplateConfigDTO.setReceiverIds(receiverIds);
        return messageTemplateConfigDTO;
    }

}
