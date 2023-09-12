package io.metersphere.project.service;


import io.metersphere.project.domain.MessageTask;
import io.metersphere.project.domain.MessageTaskBlob;
import io.metersphere.project.enums.result.ProjectResultCode;
import io.metersphere.project.mapper.MessageTaskBlobMapper;
import io.metersphere.project.mapper.MessageTaskMapper;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class NoticeMessageTaskService {
    @Resource
    private MessageTaskMapper messageTaskMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    @Resource
    private UserRoleRelationMapper userRoleRelationMapper;

    public static final String USER_IDS = "user_ids";

    public static final String NO_USER_NAMES = "no_user_names";

    public static final String CREATOR = "CREATOR";

    public static final String FOLLOW_PEOPLE = "FOLLOW_PEOPLE";


    public ResultHolder saveMessageTask(MessageTaskRequest messageTaskRequest, String userId) {
        SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH);
        MessageTaskMapper mapper = sqlSession.getMapper(MessageTaskMapper.class);
        MessageTaskBlobMapper blobMapper = sqlSession.getMapper(MessageTaskBlobMapper.class);
        //检查用户是否存在
        Map<String, List<String>> stringListMap = checkUserExistProject(messageTaskRequest.getReceiverIds(), messageTaskRequest.getProjectId());
        for (String receiverId : stringListMap.get(USER_IDS)) {
            MessageTask messageTask = new MessageTask();
            String insertId = UUID.randomUUID().toString();
            messageTask.setId(insertId);
            messageTask.setTaskType(messageTaskRequest.getTaskType());
            messageTask.setEvent(messageTaskRequest.getEvent());
            messageTask.setReceiver(receiverId);
            messageTask.setProjectId(messageTaskRequest.getProjectId());
            messageTask.setProjectRobotId(messageTaskRequest.getRobotId());
            String testId = messageTaskRequest.getTestId() == null ? "NONE" : messageTaskRequest.getTestId();
            messageTask.setTestId(testId);
            messageTask.setCreateUser(userId);
            messageTask.setCreateTime(System.currentTimeMillis());
            messageTask.setUpdateUser(userId);
            messageTask.setUpdateTime(System.currentTimeMillis());
            messageTask.setEnable(messageTaskRequest.getEnable());
            mapper.insert(messageTask);
            MessageTaskBlob messageTaskBlob = new MessageTaskBlob();
            messageTaskBlob.setId(messageTask.getId());
            messageTaskBlob.setTemplate(messageTaskRequest.getTemplate());
            blobMapper.insert(messageTaskBlob);
        }
        sqlSession.flushStatements();
        SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
        if (CollectionUtils.isNotEmpty(stringListMap.get(NO_USER_NAMES))) {
            String message = Translator.get("alert_others") + stringListMap.get(NO_USER_NAMES).get(0) + Translator.get("user.remove");
            return ResultHolder.successCodeErrorInfo(ProjectResultCode.SAVE_MESSAGE_TASK_USER_NO_EXIST.getCode(), message);
        }
        return ResultHolder.success("OK");
    }

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

}
