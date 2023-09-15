package io.metersphere.project.service;

import io.metersphere.project.domain.ProjectRobot;
import io.metersphere.project.enums.ProjectRobotPlatform;
import io.metersphere.project.mapper.ProjectRobotMapper;
import io.metersphere.system.service.CreateProjectResourceService;
import io.metersphere.system.uid.UUID;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CreateRobotResourceService implements CreateProjectResourceService {

    @Resource
    private ProjectRobotMapper robotMapper;

    @Override
    public void createResources(String projectId) {
        List<ProjectRobot> list = new ArrayList<>();
        ProjectRobot projectRobot = new ProjectRobot();
        projectRobot.setId(UUID.randomUUID().toString());
        projectRobot.setProjectId(projectId);
        projectRobot.setName("站内信");
        projectRobot.setPlatform(ProjectRobotPlatform.IN_SITE.toString());
        projectRobot.setWebhook("NONE");
        projectRobot.setCreateUser("admin");
        projectRobot.setCreateTime(System.currentTimeMillis());
        projectRobot.setUpdateUser("admin");
        projectRobot.setUpdateTime(System.currentTimeMillis());
        list.add(projectRobot);
        ProjectRobot projectRobotMail = new ProjectRobot();
        projectRobotMail.setId(UUID.randomUUID().toString());
        projectRobotMail.setProjectId(projectId);
        projectRobotMail.setName("邮件");
        projectRobotMail.setPlatform(ProjectRobotPlatform.MAIL.toString());
        projectRobotMail.setWebhook("NONE");
        projectRobotMail.setCreateUser("admin");
        projectRobotMail.setCreateTime(System.currentTimeMillis());
        projectRobotMail.setUpdateUser("admin");
        projectRobotMail.setUpdateTime(System.currentTimeMillis());
        list.add(projectRobotMail);
        robotMapper.batchInsert(list);
    }
}
