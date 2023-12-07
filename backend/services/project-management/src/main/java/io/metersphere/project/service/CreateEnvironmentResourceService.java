package io.metersphere.project.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.sdk.domain.Environment;
import io.metersphere.sdk.domain.EnvironmentExample;
import io.metersphere.sdk.mapper.EnvironmentMapper;
import io.metersphere.system.service.CreateProjectResourceService;
import io.metersphere.system.uid.IDGenerator;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CreateEnvironmentResourceService implements CreateProjectResourceService {

    public static final String MOCK_EVN_NAME = "Mock环境";
    @Resource
    private EnvironmentMapper environmentMapper;
    @Resource
    private ProjectMapper projectMapper;

    @Override
    public void createResources(String projectId) {
        // 创建默认mock环境
        EnvironmentExample example = new EnvironmentExample();
        example.createCriteria().andProjectIdEqualTo(projectId).andMockEqualTo(true);
        if (environmentMapper.countByExample(example) == 0) {
            Project project = projectMapper.selectByPrimaryKey(projectId);
            Environment environment = new Environment();
            environment.setId(IDGenerator.nextStr());
            environment.setCreateUser(project.getCreateUser());
            environment.setName(MOCK_EVN_NAME);
            environment.setMock(true);
            environment.setPos(5000L);
            environment.setProjectId(projectId);
            environment.setCreateTime(System.currentTimeMillis());
            environment.setUpdateUser(project.getCreateUser());
            environment.setUpdateTime(System.currentTimeMillis());
            environmentMapper.insert(environment);
        }
    }
}
