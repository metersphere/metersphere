package io.metersphere.project.service;

import io.metersphere.project.domain.MessageTaskExample;
import io.metersphere.project.domain.ProjectRobot;
import io.metersphere.project.domain.ProjectRobotExample;
import io.metersphere.project.dto.ProjectRobotDTO;
import io.metersphere.project.enums.ProjectRobotPlatform;
import io.metersphere.project.enums.ProjectRobotType;
import io.metersphere.project.mapper.MessageTaskMapper;
import io.metersphere.project.mapper.ProjectRobotMapper;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.sdk.util.Translator;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import io.metersphere.system.uid.UUID;

@Service
@Transactional
public class ProjectRobotService {

    @Resource
    private ProjectRobotMapper robotMapper;

    @Resource
    private MessageTaskMapper messageTaskMapper;

    public void add(ProjectRobot projectRobot) {
        projectRobot.setId(UUID.randomUUID().toString());
        projectRobot.setEnable(true);
        checkDingTalk(projectRobot);
        robotMapper.insert(projectRobot);
    }

    private static void checkDingTalk(ProjectRobot projectRobot) {
        if (StringUtils.equals(projectRobot.getPlatform(), ProjectRobotPlatform.DING_TALK.toString())) {
            if (StringUtils.isBlank(projectRobot.getType())) {
                throw new MSException(Translator.get("ding_type_is_null"));
            }
            if (StringUtils.equals(projectRobot.getType(), ProjectRobotType.ENTERPRISE.toString())) {
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
        checkDingTalk(projectRobot);
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
        messageTaskMapper.deleteByExample(messageTaskExample);
        robotMapper.deleteByPrimaryKey(id);
    }

    public void enable(String id, String updateUser, Long updateTime) {
        ProjectRobot projectRobot = checkRobotExist(id);
        if (projectRobot.getEnable()) {
            projectRobot.setEnable(false);
        } else {
            projectRobot.setEnable(true);
        }
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
        projectExample.setOrderByClause("create_time desc");

        return robotMapper.selectByExample(projectExample);
    }

    public ProjectRobotDTO getDetail(String robotId) {
        ProjectRobot projectRobotInDB = checkRobotExist(robotId);
        ProjectRobotDTO projectRobotDTO = new ProjectRobotDTO();
        BeanUtils.copyBean(projectRobotDTO, projectRobotInDB);
        return projectRobotDTO;
    }
}
