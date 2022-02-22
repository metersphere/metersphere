package io.metersphere.service;

import com.alibaba.fastjson.JSON;
import io.metersphere.base.domain.*;
import io.metersphere.base.mapper.ProjectApplicationMapper;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.commons.utils.SessionUtils;
import io.metersphere.log.utils.ReflexObjectUtil;
import io.metersphere.log.vo.DetailColumn;
import io.metersphere.log.vo.OperatingLogDetails;
import io.metersphere.log.vo.system.SystemReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectApplicationService {
    @Resource
    private ProjectApplicationMapper projectApplicationMapper;

    @Resource
    private ProjectMapper projectMapper;

    public void updateProjectApplication(ProjectApplication projectApplication){
        ProjectApplicationExample projectApplicationExample = new ProjectApplicationExample();
        projectApplicationExample.createCriteria().andProjectIdEqualTo(projectApplication.getProjectId()).andTypeEqualTo(projectApplication.getType());
        projectApplicationMapper.updateByExample(projectApplication,projectApplicationExample);
    }

    public String getLogDetails(ProjectApplication projectApplication) {
        if (projectApplication != null) {
            List<DetailColumn> columns = ReflexObjectUtil.getColumns(projectApplication, SystemReference.projectApplicationColumns);
            Project project = projectMapper.selectByPrimaryKey(projectApplication.getProjectId());
            if (project==null) {
                return null;
            }
            DetailColumn column = new DetailColumn("项目名称", "projectName", project.getName(), null);
            columns.add(column);
            OperatingLogDetails details = new OperatingLogDetails(JSON.toJSONString(project.getId()), project.getId(), "修改链接报告时间", Objects.requireNonNull(SessionUtils.getUser()).getCreateUser(), columns);
            return JSON.toJSONString(details);
        }
        return null;
    }

    public ProjectApplication getProjectApplication(String projectId, String type) {
        ProjectApplicationExample projectApplicationExample = new ProjectApplicationExample();
        projectApplicationExample.createCriteria().andProjectIdEqualTo(projectId).andTypeEqualTo(type);
        List<ProjectApplication> projectApplications = projectApplicationMapper.selectByExample(projectApplicationExample);
        if(projectApplications == null || projectApplications.size() == 0){
            return new ProjectApplication();
        }
        return projectApplications.get(0);
    }
}
