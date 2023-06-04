package io.metersphere.sdk.service;

import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class BaseProjectService {
    @Resource
    private ProjectMapper projectMapper;

    public List<Project> selectProjectList() {
        return projectMapper.selectByExample(new ProjectExample());
    }
}
