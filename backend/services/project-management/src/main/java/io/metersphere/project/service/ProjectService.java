package io.metersphere.project.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectService {
    @Resource
    private ProjectMapper projectMapper;

    public List<Project> list() {
        return projectMapper.selectByExample(new ProjectExample());
    }

    public Project add(Project project) {
        project.setId(UUID.randomUUID().toString());
        project.setCreateTime(System.currentTimeMillis());
        project.setUpdateTime(System.currentTimeMillis());
        projectMapper.insertSelective(project);
        return project;
    }

    public Project update(Project project) {
        projectMapper.updateByPrimaryKeySelective(project);
        return projectMapper.selectByPrimaryKey(project.getId());
    }
}
