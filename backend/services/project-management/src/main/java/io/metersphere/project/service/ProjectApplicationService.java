package io.metersphere.project.service;

import io.metersphere.project.domain.ProjectApplication;
import io.metersphere.project.domain.ProjectApplicationExample;
import io.metersphere.project.mapper.ProjectApplicationMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectApplicationService {
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;

    public ProjectApplication add(ProjectApplication application) {
        projectApplicationMapper.insert(application);
        return application;
    }

    public ProjectApplication update(ProjectApplication application) {
        projectApplicationMapper.updateByPrimaryKey(application);
        return application;
    }

    public List<ProjectApplication> list(String projectId) {
        ProjectApplicationExample example = new ProjectApplicationExample();
        example.createCriteria().andProjectIdEqualTo(projectId);
        return projectApplicationMapper.selectByExample(example);
    }
}
