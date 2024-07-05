package io.metersphere.project.service;

import io.metersphere.project.domain.*;
import io.metersphere.project.dto.ProjectRobotDTO;
import io.metersphere.project.mapper.MessageTaskBlobMapper;
import io.metersphere.project.mapper.MessageTaskMapper;
import io.metersphere.project.mapper.ProjectRobotMapper;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.User;
import io.metersphere.system.domain.UserExample;
import io.metersphere.system.mapper.UserMapper;
import io.metersphere.system.notice.constants.NoticeConstants;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class ProjectRobotService {

    @Resource
    private ProjectRobotMapper robotMapper;

    @Resource
    private MessageTaskMapper messageTaskMapper;

    @Resource
    private MessageTaskBlobMapper messageTaskBlobMapper;

    @Resource
    private UserMapper userMapper;

    public ProjectRobot add(ProjectRobot projectRobot) {
        projectRobot.setId(IDGenerator.nextStr());
        projectRobot.setEnable(projectRobot.getEnable());
        checkDingTalkAndSet(projectRobot);
        robotMapper.insert(projectRobot);
        return projectRobot;
    }

    private static void checkDingTalkAndSet(ProjectRobot projectRobot) {
        if (StringUtils.equals(projectRobot.getPlatform(), NoticeConstants.Type.DING_TALK)) {
            if (StringUtils.isBlank(projectRobot.getType())) {
                throw new MSException(Translator.get("ding_type_is_null"));
            }
            if (StringUtils.equals(projectRobot.getType(), NoticeConstants.DingType.ENTERPRISE)) {
                if (StringUtils.isBlank(projectRobot.getAppKey())) {
                    throw new MSException(Translator.get("ding_app_key_is_null"));
                }
                if (StringUtils.isBlank(projectRobot.getAppSecret())) {
                    throw new MSException(Translator.get("ding_app_secret_is_null"));
                }
            }
        }
    }

    public void update(ProjectRobot projectRobot) {
        checkRobotExist(projectRobot.getId());
        checkDingTalkAndSet(projectRobot);
        robotMapper.updateByPrimaryKeySelective(projectRobot);
    }

    private ProjectRobot checkRobotExist(String robotId) {
        ProjectRobot projectRobotInDB = robotMapper.selectByPrimaryKey(robotId);
        if (projectRobotInDB == null) {
            throw new MSException(Translator.get("robot_is_null"));
        }
        return projectRobotInDB;
    }

    public void delete(String id) {
        checkRobotExist(id);
        MessageTaskExample messageTaskExample = new MessageTaskExample();
        messageTaskExample.createCriteria().andProjectRobotIdEqualTo(id);
        List<MessageTask> messageTasks = messageTaskMapper.selectByExample(messageTaskExample);
        List<String> ids = messageTasks.stream().map(MessageTask::getId).toList();
        if (CollectionUtils.isNotEmpty(ids)) {
            MessageTaskBlobExample messageTaskBlobExample = new MessageTaskBlobExample();
            messageTaskBlobExample.createCriteria().andIdIn(ids);
            messageTaskBlobMapper.deleteByExample(messageTaskBlobExample);
        }
        messageTaskMapper.deleteByExample(messageTaskExample);
        robotMapper.deleteByPrimaryKey(id);
    }

    public void enable(String id, String updateUser, Long updateTime) {
        ProjectRobot projectRobot = checkRobotExist(id);
        projectRobot.setEnable(!projectRobot.getEnable());
        projectRobot.setCreateUser(null);
        projectRobot.setCreateTime(null);
        projectRobot.setUpdateUser(updateUser);
        projectRobot.setUpdateTime(updateTime);
        robotMapper.updateByPrimaryKeySelective(projectRobot);
    }

    public List<ProjectRobot> getList(String projectId) {
        ProjectRobotExample projectExample = new ProjectRobotExample();
        ProjectRobotExample.Criteria criteria = projectExample.createCriteria();
        criteria.andProjectIdEqualTo(projectId);
        List<ProjectRobot> projectRobots = robotMapper.selectByExample(projectExample);
        Map<String, String> userMap = getUserMap(projectRobots);
        for (ProjectRobot projectRobot : projectRobots) {
            if ((StringUtils.equalsIgnoreCase(projectRobot.getPlatform(), NoticeConstants.Type.IN_SITE) || StringUtils.equalsIgnoreCase(projectRobot.getPlatform(), NoticeConstants.Type.MAIL)) && StringUtils.isNotBlank(projectRobot.getDescription())) {
                projectRobot.setDescription(Translator.get(projectRobot.getDescription()));
                projectRobot.setName(Translator.get(projectRobot.getName()));
            }
            if (userMap.get(projectRobot.getCreateUser()) != null) {
                projectRobot.setCreateUser(userMap.get(projectRobot.getCreateUser()));
            }
            if (userMap.get(projectRobot.getUpdateUser()) != null) {
                projectRobot.setUpdateUser(userMap.get(projectRobot.getUpdateUser()));
            }
        }
        Map<String, List<ProjectRobot>> collect = projectRobots.stream().collect(Collectors.groupingBy(ProjectRobot::getPlatform));
        List<String> defaultPlatForm = new ArrayList<>();
        defaultPlatForm.add(NoticeConstants.Type.MAIL);
        defaultPlatForm.add(NoticeConstants.Type.IN_SITE);
        List<ProjectRobot> list = projectRobots.stream().filter(t -> !defaultPlatForm.contains(t.getPlatform())).toList();
        List<ProjectRobot> newProjectRobots = new ArrayList<>(list.stream().sorted(Comparator.comparing(ProjectRobot::getCreateTime).reversed()).toList());
        List<ProjectRobot> mailRobot = collect.get(NoticeConstants.Type.MAIL);
        if (CollectionUtils.isNotEmpty(mailRobot)) {
            newProjectRobots.add(0, mailRobot.getFirst());
        }
        List<ProjectRobot> inSiteRobot = collect.get(NoticeConstants.Type.IN_SITE);
        if (CollectionUtils.isNotEmpty(inSiteRobot)) {
            newProjectRobots.add(0, inSiteRobot.getFirst());
        }
        return newProjectRobots;
    }

    private Map<String, String> getUserMap(List<ProjectRobot> projectRobots) {
        List<String> userIds = projectRobots.stream().flatMap(projectRobot -> Stream.of(projectRobot.getCreateUser(), projectRobot.getUpdateUser())).distinct().toList();
        ;
        if (CollectionUtils.isEmpty(userIds)) {
            return new HashMap<>();
        }
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIds);
        List<User> users = userMapper.selectByExample(userExample);
        return users.stream().collect(Collectors.toMap(User::getId, User::getName));
    }

    public ProjectRobotDTO getDetail(String robotId) {
        ProjectRobot projectRobotInDB = checkRobotExist(robotId);
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        BeanUtils.copyBean(projectRobotDTO, projectRobotInDB);
        return projectRobotDTO;
    }
}
