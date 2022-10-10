package io.metersphere.service.wapper;

import io.metersphere.base.domain.Project;
import io.metersphere.base.domain.ProjectExample;
import io.metersphere.base.mapper.ProjectMapper;
import io.metersphere.commons.constants.IssuesManagePlatform;
import io.metersphere.commons.constants.ProjectApplicationType;
import io.metersphere.dto.ProjectConfig;
import io.metersphere.service.BaseProjectApplicationService;
import io.metersphere.service.BaseProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class TrackProjectService {

    @Resource
    BaseProjectApplicationService baseProjectApplicationService;
    @Resource
    BaseProjectService baseProjectService;
    @Resource
    ProjectMapper projectMapper;

    public boolean isThirdPartTemplate(Project project) {
        if (project.getThirdPartTemplate() != null && project.getThirdPartTemplate() && project.getPlatform().equals(IssuesManagePlatform.Jira.name())) {
            return true;
        }
        return false;
    }

    public boolean useCustomNum(String projectId) {
        return useCustomNum(baseProjectService.getProjectById(projectId));
    }

    public boolean useCustomNum(Project project) {
        if (project != null) {
            ProjectConfig config = baseProjectApplicationService.getSpecificTypeValue(project.getId(), ProjectApplicationType.CASE_CUSTOM_NUM.name());
            Boolean customNum = config.getCaseCustomNum();
            // 未开启自定义ID
            if (!customNum) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    public List<String> getThirdPartProjectIds() {
        ProjectExample example = new ProjectExample();
        example.createCriteria().andPlatformNotEqualTo(IssuesManagePlatform.Local.name());
        return projectMapper.selectByExample(example)
                .stream()
                .map(Project::getId)
                .collect(Collectors.toList());
    }
}
