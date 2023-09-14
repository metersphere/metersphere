package io.metersphere.project.service;


import io.metersphere.project.domain.*;
import io.metersphere.project.dto.MessageTaskDTO;
import io.metersphere.project.dto.ProjectRobotConfigDTO;
import io.metersphere.project.enums.ProjectRobotPlatform;
import io.metersphere.project.enums.result.ProjectResultCode;
import io.metersphere.project.mapper.MessageTaskBlobMapper;
import io.metersphere.project.mapper.MessageTaskMapper;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.mapper.ProjectRobotMapper;
import io.metersphere.sdk.controller.handler.ResultHolder;
import io.metersphere.sdk.dto.request.MessageTaskRequest;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.uid.UUID;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserRoleRelation;
import io.metersphere.system.domain.UserRoleRelationExample;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.mapper.UserRoleRelationMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    public static final String USER_IDS = "user_ids";

    public static final String NO_USER_NAMES = "no_user_names";

    public static final String CREATOR = "CREATOR";

    public static final String FOLLOW_PEOPLE = "FOLLOW_PEOPLE";


    public ResultHolder saveMessageTask(MessageTaskRequest messageTaskRequest, String userId) {
        String projectId = messageTaskRequest.getProjectId();
        checkProjectExist(projectId);
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        MessageTaskMapper mapper = sqlSession.getMapper(MessageTaskMapper.class);
        MessageTaskBlobMapper blobMapper = sqlSession.getMapper(MessageTaskBlobMapper.class);
        //检查用户是否存在
        Map<String, List<String>> stringListMap = checkUserExistProject(messageTaskRequest.getReceiverIds(), projectId);
        List<String> existUserIds = stringListMap.get(USER_IDS);
        //如果只选了用户，没有选机器人，默认机器人为站内信
        String robotId = setDefaultRobot(messageTaskRequest.getProjectId(), messageTaskRequest.getRobotId());
        messageTaskRequest.setRobotId(robotId);
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

    private void checkProjectExist(String projectId) {
        Project project = projectMapper.selectByPrimaryKey(projectId);
        if (project == null) {
            throw new MSException(Translator.get("project_is_not_exist"));
        }
    }

    /**
     * 新增MessageTask
     * @param messageTaskRequest 入参
     * @param userId 当前用户i的
     * @param mapper MessageTaskMapper
     * @param blobMapper MessageTaskBlobMapper
     * @param existUserIds 系统中还存在的入参传过来的接收人
     * @param messageTaskReceivers 更新过后还有多少接收人需要保存
     */
    private static void insertMessageTask(MessageTaskRequest messageTaskRequest, String userId, MessageTaskMapper mapper, MessageTaskBlobMapper blobMapper, List<String> existUserIds, List<String> messageTaskReceivers) {
        String testId = messageTaskRequest.getTestId() == null ? "NONE" : messageTaskRequest.getTestId();
        boolean enable = messageTaskRequest.getEnable() != null && messageTaskRequest.getEnable();
        for (String receiverId : existUserIds) {
            if (CollectionUtils.isNotEmpty(messageTaskReceivers) && messageTaskReceivers.contains(receiverId)) {
                continue;
            }
            MessageTask messageTask = new MessageTask();
            buildMessageTask(messageTaskRequest, userId, messageTaskRequest.getRobotId(), testId, enable, receiverId, messageTask);
            mapper.insert(messageTask);
            MessageTaskBlob messageTaskBlob = new MessageTaskBlob();
            messageTaskBlob.setId(messageTask.getId());
            messageTaskBlob.setTemplate(messageTaskRequest.getTemplate());
            blobMapper.insert(messageTaskBlob);
        }
    }

    /**
     * 查询默认机器人id
     * @param projectId 项目id
     * @param robotId 机器人id
     * @return String
     */
    private String setDefaultRobot(String projectId, String robotId) {
        if (StringUtils.isBlank(robotId)) {
            ProjectRobotExample projectRobotExample = new ProjectRobotExample();
            projectRobotExample.createCriteria().andProjectIdEqualTo(projectId).andPlatformEqualTo(ProjectRobotPlatform.IN_SITE.toString());
            List<ProjectRobot> projectRobots = projectRobotMapper.selectByExample(projectRobotExample);
            robotId = projectRobots.get(0).getId();
        }
        return robotId;
    }

    /**
     * 检查数据库是否有同类型数据，有则更新
     * @param messageTaskRequest 入参
     * @param userId 当前用户ID
     * @param mapper MessageTaskMapper
     * @param blobMapper MessageTaskBlobMapper
     * @param existUserIds 系统中还存在的入参传过来的接收人
     * @return List<MessageTask>
     */
    private List<MessageTask> updateMessageTasks(MessageTaskRequest messageTaskRequest, String userId, MessageTaskMapper mapper, MessageTaskBlobMapper blobMapper, List<String> existUserIds) {
        boolean enable = messageTaskRequest.getEnable() != null && messageTaskRequest.getEnable();
        MessageTaskExample messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andReceiverIn(existUserIds).andProjectIdEqualTo(messageTaskRequest.getProjectId())
                .andProjectRobotIdEqualTo(messageTaskRequest.getRobotId()).andTaskTypeEqualTo(messageTaskRequest.getTaskType()).andEventEqualTo(messageTaskRequest.getEvent());
        List<MessageTask> messageTasks = messageTaskMapper.selectByExample(messageTaskExample);
        if (CollectionUtils.isEmpty(messageTasks)) {
            return new ArrayList<>();
        }
        for (MessageTask messageTask : messageTasks) {
            messageTask.setUpdateTime(System.currentTimeMillis());
            messageTask.setUpdateUser(userId);
            messageTask.setEnable(enable);
            mapper.updateByPrimaryKeySelective(messageTask);
        }
        List<String> messageTaskIds = messageTasks.stream().map(MessageTask::getId).toList();
        MessageTaskBlobExample messageTaskBlobExample = new MessageTaskBlobExample();
        messageTaskBlobExample.createCriteria().andIdIn(messageTaskIds);
        List<MessageTaskBlob> messageTaskBlobs = messageTaskBlobMapper.selectByExample(messageTaskBlobExample);
        for (MessageTaskBlob messageTaskBlob : messageTaskBlobs) {
            messageTaskBlob.setTemplate(messageTaskRequest.getTemplate());
            blobMapper.updateByPrimaryKeySelective(messageTaskBlob);
        }
        return messageTasks;
    }

    private static void buildMessageTask(MessageTaskRequest messageTaskRequest, String userId, String robotId, String testId, boolean enable, String receiverId, MessageTask messageTask) {
        String insertId = UUID.randomUUID().toString();
        messageTask.setId(insertId);
        messageTask.setTaskType(messageTaskRequest.getTaskType());
        messageTask.setEvent(messageTaskRequest.getEvent());
        messageTask.setReceiver(receiverId);
        messageTask.setProjectId(messageTaskRequest.getProjectId());
        messageTask.setProjectRobotId(robotId);
        messageTask.setTestId(testId);
        messageTask.setCreateUser(userId);
        messageTask.setCreateTime(System.currentTimeMillis());
        messageTask.setUpdateUser(userId);
        messageTask.setUpdateTime(System.currentTimeMillis());
        messageTask.setEnable(enable);
    }

    /**
     * 检查用户是否存在
     * @param receiverIds 接收人ids
     * @param projectId 项目id
     * @return Map<String, List<String>>
     */
    private Map<String, List<String>> checkUserExistProject(List<String> receiverIds, String projectId) {
        UserRoleRelationExample userRoleRelationExample = new UserRoleRelationExample();
        userRoleRelationExample.createCriteria().andUserIdIn(receiverIds).andSourceIdEqualTo(projectId);
        List<UserRoleRelation> userRoleRelations = userRoleRelationMapper.selectByExample(userRoleRelationExample);
        List<String> userIds = userRoleRelations.stream().map(UserRoleRelation::getUserId).distinct().toList();
        Map<String, List<String>> map = new HashMap<>();
        if (CollectionUtils.isEmpty(userIds)) {
            throw new MSException(Translator.get("user.not.exist"));
        }
        List<String> noUserNames = new ArrayList<>();
        if (userIds.size() < receiverIds.size()) {
            for (String receiverId : receiverIds) {
                if (!StringUtils.equalsIgnoreCase(receiverId, CREATOR) && !StringUtils.equalsIgnoreCase(receiverId, FOLLOW_PEOPLE) && !userIds.contains(receiverId)) {
                    User user = userMapper.selectByPrimaryKey(receiverId);
                    noUserNames.add(user.getName());
                    break;
                }
            }
        }
        map.put(NO_USER_NAMES, noUserNames);
        map.put(USER_IDS, userIds);
        return map;
    }

    public List<MessageTaskDTO> getMessageList(String projectId) {
        checkProjectExist(projectId);
        MessageTaskExample messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andProjectIdEqualTo(projectId);
        List<MessageTask> messageTasks = messageTaskMapper.selectByExample(messageTaskExample);
        if (CollectionUtils.isEmpty(messageTasks)) {
            return new ArrayList<>();
        }
        List<String> messageTaskIds = messageTasks.stream().map(MessageTask::getId).toList();
        MessageTaskBlobExample messageTaskBlobExample = new MessageTaskBlobExample();
        messageTaskBlobExample.createCriteria().andIdIn(messageTaskIds);
        List<MessageTaskBlob> messageTaskBlobs = messageTaskBlobMapper.selectByExample(messageTaskBlobExample);
        Map<String, MessageTaskBlob> messageTaskBlobMap = messageTaskBlobs.stream().collect(Collectors.toMap(MessageTaskBlob::getId, item -> item));
        Map<String, List<MessageTask>> messageMap = messageTasks.stream().collect(Collectors.groupingBy(t -> (t.getTaskType() + "-" + t.getEvent())));
        List<MessageTaskDTO> list = new ArrayList<>();
        messageMap.forEach((key, messageTaskList) -> {
            MessageTaskDTO messageTaskDTO = new MessageTaskDTO();
            int i = key.indexOf("-");
            String taskType = key.substring(0, i);
            String event = key.substring(i+1);
            messageTaskDTO.setProjectId(projectId);
            messageTaskDTO.setTaskType(taskType);
            messageTaskDTO.setEvent(event);
            Set<String>receiverIds = new HashSet<>();
            List<ProjectRobotConfigDTO>projectRobotConfigList = new ArrayList<>();
            for (MessageTask messageTask : messageTaskList) {
                MessageTaskBlob messageTaskBlob = messageTaskBlobMap.get(messageTask.getId());
                receiverIds.add(messageTask.getReceiver());
                ProjectRobotConfigDTO projectRobotConfigDTO = new ProjectRobotConfigDTO();
                projectRobotConfigDTO.setRobotId(messageTask.getProjectRobotId());
                projectRobotConfigDTO.setEnable(messageTask.getEnable());
                projectRobotConfigDTO.setTemplate(messageTaskBlob.getTemplate());
                projectRobotConfigList.add(projectRobotConfigDTO);
            }
            messageTaskDTO.setReceiverIds(new ArrayList<>(receiverIds));
            messageTaskDTO.setProjectRobotConfigList(projectRobotConfigList);
            list.add(messageTaskDTO);
        });
        return list;
    }
}
