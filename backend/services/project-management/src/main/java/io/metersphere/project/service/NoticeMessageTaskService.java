package io.metersphere.project.service;


import io.metersphere.project.domain.*;
import io.metersphere.project.dto.*;
import io.metersphere.project.enums.result.ProjectResultCode;
import io.metersphere.project.mapper.ExtProjectUserRoleMapper;
import io.metersphere.project.mapper.MessageTaskBlobMapper;
import io.metersphere.project.mapper.MessageTaskMapper;
import io.metersphere.project.mapper.ProjectRobotMapper;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.JSON;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.controller.handler.ResultHolder;
import io.metersphere.system.domain.*;
import io.metersphere.system.dto.sdk.OptionDTO;
import io.metersphere.system.dto.sdk.request.MessageTaskRequest;
import io.metersphere.system.mapper.CustomFieldMapper;
import io.metersphere.system.mapper.ExtSystemProjectMapper;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.notice.utils.MessageTemplateUtils;
import io.metersphere.system.uid.IDGenerator;
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
    private ExtProjectUserRoleMapper extProjectUserRoleMapper;
    @Resource
    protected CustomFieldMapper customFieldMapper;
    @Resource
    private ExtSystemProjectMapper extSystemProjectMapper;


    public static final String USER_IDS = "user_ids";

    public static final String NO_USER_NAMES = "no_user_names";


    public ResultHolder saveMessageTask(MessageTaskRequest messageTaskRequest, String userId) {
        String projectId = messageTaskRequest.getProjectId();
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        MessageTaskMapper mapper = sqlSession.getMapper(MessageTaskMapper.class);
        MessageTaskBlobMapper blobMapper = sqlSession.getMapper(MessageTaskBlobMapper.class);
        //检查用户是否存在
        Map<String, List<String>> stringListMap = checkUserExistProject(messageTaskRequest.getReceiverIds(), projectId);
        List<String> existUserIds = stringListMap.get(USER_IDS);
        //检查设置的通知是否存在，如果存在则更新
        List<MessageTask> messageTasks = updateMessageTasks(messageTaskRequest, userId, mapper, blobMapper, existUserIds);
        //不存在则新增
        if (CollectionUtils.isEmpty(messageTasks)) {
            insertMessageTask(messageTaskRequest, userId, mapper, blobMapper, existUserIds);
        }

        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        if (CollectionUtils.isNotEmpty(stringListMap.get(NO_USER_NAMES))) {
            String message = Translator.get("alert_others") + stringListMap.get(NO_USER_NAMES).getFirst() + Translator.get("user.remove");
            return ResultHolder.successCodeErrorInfo(ProjectResultCode.SAVE_MESSAGE_TASK_USER_NO_EXIST.getCode(), message);
        }
        return ResultHolder.success("OK");
    }

    /**
     * 新增MessageTask
     *
     * @param messageTaskRequest 入参
     * @param userId             当前用户i的
     * @param mapper             MessageTaskMapper
     * @param blobMapper         MessageTaskBlobMapper
     * @param existUserIds       系统中还存在的入参传过来的接收人
     */
    private void insertMessageTask(MessageTaskRequest messageTaskRequest, String userId, MessageTaskMapper mapper, MessageTaskBlobMapper blobMapper, List<String> existUserIds) {
        //如果新增时只选了用户，没有选机器人，默认机器人为站内信
        ProjectRobot projectRobot = getDefaultRobot(messageTaskRequest.getProjectId(), messageTaskRequest.getRobotId());
        String robotId = projectRobot.getId();
        messageTaskRequest.setRobotId(robotId);
        MessageTask messageTask = new MessageTask();
        buildMessageTask(messageTaskRequest, userId, messageTask, existUserIds);
        mapper.insert(messageTask);
        MessageTaskBlob messageTaskBlob = new MessageTaskBlob();
        messageTaskBlob.setId(messageTask.getId());
        if (!messageTask.getUseDefaultTemplate()) {
            messageTaskBlob.setTemplate(messageTaskRequest.getTemplate());
        }
        messageTaskBlob.setTemplate(messageTaskRequest.getTemplate());
        blobMapper.insert(messageTaskBlob);

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
            projectRobotExample.createCriteria().andProjectIdEqualTo(projectId).andPlatformEqualTo(NoticeConstants.Type.IN_SITE);
            List<ProjectRobot> projectRobots = projectRobotMapper.selectByExample(projectRobotExample);
            return projectRobots.getFirst();
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
     */
    private List<MessageTask> updateMessageTasks(MessageTaskRequest messageTaskRequest, String userId, MessageTaskMapper mapper, MessageTaskBlobMapper blobMapper, List<String> existUserIds) {
        boolean enable = getBooleanValue(false, messageTaskRequest.getEnable());
        boolean useDefaultSubject = getBooleanValue(true, messageTaskRequest.getUseDefaultSubject());
        boolean useDefaultTemplate = getBooleanValue(true, messageTaskRequest.getUseDefaultTemplate());
        //查询在当前事件和已存在的通知人员下的数据，即数据库已存在的数据
        MessageTaskExample messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andProjectIdEqualTo(messageTaskRequest.getProjectId()).andTaskTypeEqualTo(messageTaskRequest.getTaskType()).andEventEqualTo(messageTaskRequest.getEvent());
        List<MessageTask> messageTasks = messageTaskMapper.selectByExample(messageTaskExample);
        if (CollectionUtils.isEmpty(messageTasks)) {
            return new ArrayList<>();
        }
        //如果没有机器人id,则更新人，如果有机器人id,过滤已存在的是否有当前机器人
        if (StringUtils.isNotBlank(messageTaskRequest.getRobotId())) {
            List<MessageTask> list = messageTasks.stream().filter(t -> StringUtils.equalsIgnoreCase(t.getProjectRobotId(), messageTaskRequest.getRobotId())).toList();
            if (CollectionUtils.isEmpty(list)) {
                return new ArrayList<>();
            }
        }
        List<String> messageTaskIds = messageTasks.stream().map(MessageTask::getId).toList();
        MessageTaskBlobExample messageTaskBlobExample = new MessageTaskBlobExample();
        messageTaskBlobExample.createCriteria().andIdIn(messageTaskIds);
        List<MessageTaskBlob> messageTaskBlobs = messageTaskBlobMapper.selectByExample(messageTaskBlobExample);
        List<String>messageTaskEqualsRobotIDs = new ArrayList<>();
        for (MessageTask messageTask : messageTasks) {
            messageTask.setUpdateTime(System.currentTimeMillis());
            messageTask.setUpdateUser(userId);
            //如果有机器人id,则是修改机器人开关和消息配置
            if (StringUtils.isNotBlank(messageTaskRequest.getRobotId()) && StringUtils.equalsIgnoreCase(messageTask.getProjectRobotId(), messageTaskRequest.getRobotId())) {
                messageTaskEqualsRobotIDs.add(messageTask.getId());
                messageTask.setEnable(enable);
                messageTask.setUseDefaultSubject(useDefaultSubject);
                messageTask.setUseDefaultTemplate(useDefaultTemplate);
                if (!useDefaultSubject) {
                    messageTask.setSubject(messageTaskRequest.getSubject());
                }
            }
            messageTask.setReceivers(existUserIds);
            mapper.updateByPrimaryKeySelective(messageTask);
        }
        for (MessageTaskBlob messageTaskBlob : messageTaskBlobs) {
            if (StringUtils.isNotBlank(messageTaskRequest.getRobotId()) && !useDefaultTemplate && messageTaskEqualsRobotIDs.contains(messageTaskBlob.getId())) {
                messageTaskBlob.setTemplate(messageTaskRequest.getTemplate());
                blobMapper.updateByPrimaryKeyWithBLOBs(messageTaskBlob);
            }
        }
        return messageTasks;
    }

    private static boolean getBooleanValue(boolean value, Boolean messageTaskRequest) {
        boolean enable = value;
        if (messageTaskRequest != null) {
            enable = messageTaskRequest;
        }
        return enable;
    }

    private static void buildMessageTask(MessageTaskRequest messageTaskRequest, String userId, MessageTask messageTask, List<String> existUserIds) {
        String testId = messageTaskRequest.getTestId() == null ? "NONE" : messageTaskRequest.getTestId();
        boolean enable = getBooleanValue(false, messageTaskRequest.getEnable());
        boolean useDefaultSubject = getBooleanValue(true, messageTaskRequest.getUseDefaultSubject());
        boolean useDefaultTemplate = getBooleanValue(true, messageTaskRequest.getUseDefaultTemplate());
        String insertId = IDGenerator.nextStr();
        messageTask.setId(insertId);
        messageTask.setTaskType(messageTaskRequest.getTaskType());
        messageTask.setEvent(messageTaskRequest.getEvent());
        messageTask.setReceivers(existUserIds);
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
        if (receiverIds.contains(NoticeConstants.RelatedUser.HANDLE_USER)) {
            userIds.add(NoticeConstants.RelatedUser.HANDLE_USER);
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
     * 获取自定义字段的解释
     *
     * @return Map<String, String>
     */
    public Map<String, List<CustomField>> getCustomFielddMap(String projectId) {
        List<String> sceneList = new ArrayList<>();
        sceneList.add(TemplateScene.API.toString());
        sceneList.add(TemplateScene.TEST_PLAN.toString());
        sceneList.add(TemplateScene.FUNCTIONAL.toString());
        sceneList.add(TemplateScene.BUG.toString());
        sceneList.add(TemplateScene.UI.toString());
        CustomFieldExample example = new CustomFieldExample();
        example.createCriteria().andScopeIdEqualTo(projectId).andSceneIn(sceneList);
        List<CustomField> customFields = customFieldMapper.selectByExample(example);
        return customFields.stream().collect(Collectors.groupingBy(CustomField::getScene));
    }

    /**
     * 根据项目id 获取当前项目的消息设置
     *
     * @param projectId 项目ID
     * @return List<MessageTaskDTO>
     */
    public List<MessageTaskDTO> getMessageList(String projectId) throws IOException {
        //获取返回数据结构
        StringBuilder jsonStr = new StringBuilder();
        try {
            InputStream inputStream = getClass().getResourceAsStream("/message_task.json");
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
        List<String> robotIds = new ArrayList<>();
        List<String> userIds = new ArrayList<>();
        messageTasks.forEach(t->{
            robotIds.add(t.getProjectRobotId());
            userIds.addAll(t.getReceivers());
        });
        ProjectRobotExample projectRobotExample = new ProjectRobotExample();
        projectRobotExample.createCriteria().andIdIn(robotIds);
        List<ProjectRobot> projectRobots = projectRobotMapper.selectByExample(projectRobotExample);
        Map<String, ProjectRobot> robotMap = projectRobots.stream().collect(Collectors.toMap(ProjectRobot::getId, item -> item));
        List<User> users = extSystemProjectMapper.getProjectMemberByUserId(projectId, userIds);
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
        Map<String, List<CustomField>> customFielddMap = getCustomFielddMap(projectId);
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
                List<String> eventList = List.of(NoticeConstants.Event.CREATE, NoticeConstants.Event.UPDATE, NoticeConstants.Event.DELETE);
                for (MessageTaskDetailDTO messageTaskDetailDTO : messageTaskTypeDTO.getMessageTaskDetailDTOList()) {
                    if (StringUtils.equalsIgnoreCase(messageTaskTypeDTO.getTaskType(), NoticeConstants.TaskType.API_DEFINITION_TASK) && eventList.contains(messageTaskDetailDTO.getEvent())) {
                        messageTaskDetailDTO.setEventName("API" + StringUtils.SPACE + eventMap.get(messageTaskDetailDTO.getEvent()));
                    } else {
                        messageTaskDetailDTO.setEventName(eventMap.get(messageTaskDetailDTO.getEvent()));
                    }
                    List<MessageTask> messageTaskList = messageEventMap.get(messageTaskDetailDTO.getEvent());
                    List<OptionDTO> receivers = new ArrayList<>();
                    Map<String, ProjectRobotConfigDTO> projectRobotConfigMap = new HashMap<>();
                    String defaultTemplate = defaultTemplateMap.get(messageTaskTypeDTO.getTaskType() + "_" + messageTaskDetailDTO.getEvent());
                    if (CollectionUtils.isEmpty(messageTaskList)) {
                        String defaultSubject = defaultTemplateSubjectMap.get(messageTaskTypeDTO.getTaskType() + "_" + messageTaskDetailDTO.getEvent());
                        ProjectRobotConfigDTO projectRobotConfigDTO = getDefaultProjectRobotConfigDTO(messageTaskTypeDTO.getTaskType(), defaultTemplate, defaultSubject, projectRobot, customFielddMap);
                        projectRobotConfigMap.put(projectRobot.getId(), projectRobotConfigDTO);
                    } else {
                        for (MessageTask messageTask : messageTaskList) {
                            MessageTaskBlob messageTaskBlob = messageTaskBlobMap.get(messageTask.getId());
                            List<String> receiverIds = messageTask.getReceivers();
                            for (String receiverId : receiverIds) {
                                if (userNameMap.get(receiverId)!=null) {
                                    OptionDTO optionDTO = new OptionDTO();
                                    optionDTO.setId(receiverId);
                                    optionDTO.setName(userNameMap.get(receiverId));
                                    receivers.add(optionDTO);
                                }
                            }
                            String platform = robotMap.get(messageTask.getProjectRobotId()).getPlatform();
                            String defaultSubject;
                            if (StringUtils.equalsIgnoreCase(platform, NoticeConstants.Type.MAIL)) {
                                defaultSubject = "MeterSphere " + defaultTemplateSubjectMap.get(messageTaskTypeDTO.getTaskType() + "_" + messageTaskDetailDTO.getEvent());
                            } else {
                                defaultSubject = defaultTemplateSubjectMap.get(messageTaskTypeDTO.getTaskType() + "_" + messageTaskDetailDTO.getEvent());
                            }
                            ProjectRobotConfigDTO projectRobotConfigDTO = getProjectRobotConfigDTO(defaultTemplate, defaultSubject, robotMap.get(messageTask.getProjectRobotId()), messageTask, messageTaskBlob, customFielddMap);
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

    private ProjectRobotConfigDTO getProjectRobotConfigDTO(String defaultTemplate, String defaultSubject, ProjectRobot projectRobot, MessageTask messageTask, MessageTaskBlob messageTaskBlob, Map<String, List<CustomField>> customFielddMap) {
        ProjectRobotConfigDTO projectRobotConfigDTO = new ProjectRobotConfigDTO();
        if (StringUtils.equalsIgnoreCase(projectRobot.getName(), "robot_in_site") || StringUtils.equalsIgnoreCase(projectRobot.getName(), "robot_mail")) {
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
        String translateTemplate = MessageTemplateUtils.getTranslateTemplate(messageTask.getTaskType(), projectRobotConfigDTO.getTemplate(), customFielddMap);
        String translateSubject = MessageTemplateUtils.getTranslateSubject(messageTask.getTaskType(), projectRobotConfigDTO.getSubject(), customFielddMap);
        projectRobotConfigDTO.setPreviewTemplate(translateTemplate);
        projectRobotConfigDTO.setPreviewSubject(translateSubject);
        projectRobotConfigDTO.setDefaultTemplate(defaultTemplate);
        projectRobotConfigDTO.setDefaultSubject(defaultSubject);
        projectRobotConfigDTO.setUseDefaultSubject(messageTask.getUseDefaultSubject());
        projectRobotConfigDTO.setUseDefaultTemplate(messageTask.getUseDefaultTemplate());
        return projectRobotConfigDTO;
    }

    private static ProjectRobotConfigDTO getDefaultProjectRobotConfigDTO(String taskType, String defaultTemplate, String defaultSubject, ProjectRobot projectRobot, Map<String, List<CustomField>> customFielddMap) {
        ProjectRobotConfigDTO projectRobotConfigDTO = new ProjectRobotConfigDTO();
        projectRobotConfigDTO.setRobotId(projectRobot.getId());
        projectRobotConfigDTO.setRobotName(Translator.get(projectRobot.getName()));
        projectRobotConfigDTO.setPlatform(NoticeConstants.Type.IN_SITE);
        projectRobotConfigDTO.setDingType(projectRobot.getType());
        projectRobotConfigDTO.setEnable(false);
        projectRobotConfigDTO.setTemplate(defaultTemplate);
        projectRobotConfigDTO.setDefaultTemplate(defaultTemplate);
        projectRobotConfigDTO.setSubject(defaultSubject);
        projectRobotConfigDTO.setDefaultSubject(defaultSubject);
        projectRobotConfigDTO.setUseDefaultSubject(true);
        projectRobotConfigDTO.setUseDefaultTemplate(true);
        String translateTemplate = MessageTemplateUtils.getTranslateTemplate(taskType, defaultTemplate, customFielddMap);
        String translateSubject = MessageTemplateUtils.getTranslateSubject(taskType, defaultSubject, customFielddMap);
        projectRobotConfigDTO.setPreviewTemplate(translateTemplate);
        projectRobotConfigDTO.setPreviewSubject(translateSubject);
        return projectRobotConfigDTO;
    }

    public List<OptionDTO> getUserList(String projectId, String keyword) {
        List<OptionDTO> projectUserSelectList = extProjectUserRoleMapper.getProjectUserSelectList(projectId, keyword);
        ArrayList<OptionDTO> collect = projectUserSelectList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(OptionDTO::getId))), ArrayList::new));
        Map<String, String> defaultRelatedUserMap = MessageTemplateUtils.getDefaultRelatedUserMap();
        defaultRelatedUserMap.forEach((k, v) -> {
            OptionDTO optionDTO = new OptionDTO();
            optionDTO.setId(k);
            optionDTO.setName(v);
            collect.add(optionDTO);
        });
        return collect;
    }


    public MessageTemplateConfigDTO getTemplateDetail(String projectId, String taskType, String event, String robotId) {
        MessageTaskExample messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andProjectIdEqualTo(projectId).andTaskTypeEqualTo(taskType).andEventEqualTo(event);
        List<MessageTask> messageTasks = messageTaskMapper.selectByExample(messageTaskExample);
        Map<String, String> defaultTemplateMap = MessageTemplateUtils.getDefaultTemplateMap();
        Map<String, String> defaultTemplateSubjectMap = MessageTemplateUtils.getDefaultTemplateSubjectMap();
        Map<String, List<CustomField>> customFielddMap = getCustomFielddMap(projectId);
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
        Map<String, List<MessageTask>> messageRobotMap = messageTasks.stream().collect(Collectors.groupingBy(MessageTask::getProjectRobotId));
        if (CollectionUtils.isNotEmpty(messageRobotMap.get(robotId))) {
            messageTask = messageRobotMap.get(robotId).getFirst();
        } else {
            messageTask = messageTasks.getFirst();
            messageTask.setEnable(false);
            messageTask.setUseDefaultTemplate(true);
            messageTask.setUseDefaultSubject(true);
        }
        MessageTaskBlob messageTaskBlob = messageTaskBlobMapper.selectByPrimaryKey(messageTask.getId());
        String defaultTemplate = defaultTemplateMap.get(messageTask.getTaskType() + "_" + messageTask.getEvent());
        String defaultSubject = defaultTemplateSubjectMap.get(messageTask.getTaskType() + "_" + messageTask.getEvent());
        ProjectRobotConfigDTO projectRobotConfigDTO = getProjectRobotConfigDTO(defaultTemplate, defaultSubject, projectRobot, messageTask, messageTaskBlob, customFielddMap);
        MessageTemplateConfigDTO messageTemplateConfigDTO = new MessageTemplateConfigDTO();
        BeanUtils.copyBean(messageTemplateConfigDTO, projectRobotConfigDTO);
        Map<String, String> taskTypeMap = MessageTemplateUtils.getTaskTypeMap();
        Map<String, String> eventMap = MessageTemplateUtils.getEventMap();
        messageTemplateConfigDTO.setTaskTypeName(taskTypeMap.get(messageTask.getTaskType()));
        messageTemplateConfigDTO.setEventName(eventMap.get(messageTask.getEvent()));
        messageTemplateConfigDTO.setReceiverIds(messageTask.getReceivers());
        return messageTemplateConfigDTO;
    }

}
