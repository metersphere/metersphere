package io.metersphere.service;

import io.metersphere.base.domain.Project;
import io.metersphere.base.mapper.ProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectService {
    @Resource
    private ProjectMapper projectMapper;

    public Project addProject(Project project) {
        project.setId(UUID.randomUUID().toString());
        long createTime = System.currentTimeMillis();
        project.setCreateTime(createTime);
        project.setUpdateTime(createTime);
        // todo set workspace id
//        project.setWorkspaceId();
        projectMapper.insertSelective(project);
        return project;
    }

    public List<Project> getProjectList() {
        // todo 查询条件设置
        return projectMapper.selectByExample(null);
    }

    public void deleteProject(String projectId) {
        projectMapper.deleteByPrimaryKey(projectId);
    }

    public void updateProject(Project project) {
        project.setCreateTime(null);// 创建时间禁止修改
        project.setUpdateTime(System.currentTimeMillis());
        projectMapper.updateByPrimaryKeySelective(project);
    }

    public List<Project> listAll() {
        return projectMapper.selectByExample(null);
    }
}
