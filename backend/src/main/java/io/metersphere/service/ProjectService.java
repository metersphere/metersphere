package io.metersphere.service;

import io.metersphere.base.domain.Project;
import io.metersphere.base.mapper.ProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectService {
    @Resource
    private ProjectMapper projectMapper;

    public List<Project> listAll() {
        return projectMapper.selectByExample(null);
    }
}
