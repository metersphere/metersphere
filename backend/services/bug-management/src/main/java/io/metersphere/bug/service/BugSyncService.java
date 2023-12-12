package io.metersphere.bug.service;

import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.domain.BugExample;
import io.metersphere.bug.dto.request.BugSyncRequest;
import io.metersphere.bug.enums.BugPlatform;
import io.metersphere.bug.mapper.BugMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.domain.ProjectExample;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.LogUtils;
import io.metersphere.system.domain.Template;
import io.metersphere.system.domain.TemplateExample;
import io.metersphere.system.mapper.TemplateMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author song-cc-rock
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BugSyncService {

    @Resource
    private BugMapper bugMapper;
    @Resource
    private BugService bugService;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private TemplateMapper templateMapper;
    @Resource
    private BugSyncExtraService bugSyncExtraService;
    @Resource
    private ProjectTemplateService projectTemplateService;
    @Resource
    private ProjectApplicationService projectApplicationService;

    /**
     * XPACK用户 (同步全量缺陷)
     * @param request 同步全量参数
     */
    public void syncAllBugs(BugSyncRequest request) {
        try {
            // 获取当前项目同步缺陷唯一Key
            String syncValue = bugSyncExtraService.getSyncKey(request.getProjectId());
            if (StringUtils.isEmpty(syncValue)) {
                // 不存在, 设置保证唯一性, 并开始同步
                bugSyncExtraService.setSyncKey(request.getProjectId());
                Project project = getProjectById(request.getProjectId());
                bugService.syncPlatformAllBugs(request, project);
            }
        } catch (Exception e) {
            bugSyncExtraService.deleteSyncKey(request.getProjectId());
            throw new MSException(e);
        }
    }

    /**
     * 开源用户 (同步存量缺陷)
     * @param projectId 项目ID
     */
    public void syncBugs(String projectId) {
        try {
            String syncValue = bugSyncExtraService.getSyncKey(projectId);
            if (StringUtils.isEmpty(syncValue)) {
                bugSyncExtraService.setSyncKey(projectId);
                Project project = getProjectById(projectId);
                String platformName = projectApplicationService.getPlatformName(projectId);
                if (StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
                    // 当前项目为本地平台, 不需要同步第三方
                    bugSyncExtraService.deleteSyncKey(projectId);
                }
                // 查询存量的平台缺陷
                BugExample example = new BugExample();
                example.createCriteria().andProjectIdEqualTo(projectId).andPlatformEqualTo(platformName).andDeletedEqualTo(false);
                List<Bug> bugs = bugMapper.selectByExample(example);
                if (CollectionUtils.isEmpty(bugs)) {
                    bugSyncExtraService.deleteSyncKey(projectId);
                } else {
                    Template pluginTemplate = projectTemplateService.getPluginBugTemplate(projectId);
                    List<String> templateIds = bugs.stream().map(Bug::getTemplateId).toList();
                    TemplateExample templateExample = new TemplateExample();
                    templateExample.createCriteria().andScopeIdEqualTo(projectId).andSceneEqualTo(TemplateScene.BUG.name()).andIdIn(templateIds);
                    List<Template> templates = templateMapper.selectByExample(templateExample);
                    Map<String, Template> templateMap = templates.stream().collect(Collectors.toMap(Template::getId, t -> t));
                    // 非插件默认模板且模板不存在, 无需同步
                    bugs.removeIf(bug -> !templateMap.containsKey(bug.getTemplateId()) && !StringUtils.equals(bug.getTemplateId(), pluginTemplate.getId()));
                    bugService.syncPlatformBugs(bugs, project);
                }
            }
        } catch (Exception e) {
            bugSyncExtraService.deleteSyncKey(projectId);
            throw new MSException(e);
        }
    }

    /**
     * 定时任务同步缺陷(存量)
     */
    public void syncPlatformBugBySchedule() {
        ProjectExample example = new ProjectExample();
        List<Project> projects = projectMapper.selectByExample(example);
        List<String> allProjectIds = projects.stream().map(Project::getId).toList();
        List<String> syncProjectIds = projectApplicationService.filterNoLocalPlatform(allProjectIds);
        syncProjectIds.forEach(id -> {
            try {
                syncBugs(id);
            } catch (Exception e) {
                LogUtils.error(e.getMessage(), e);
            }
        });
    }

    /**
     * 根据项目ID获取项目信息
     * @param projectId 项目ID
     * @return 项目信息
     */
    private Project getProjectById(String projectId) {
        return projectMapper.selectByPrimaryKey(projectId);
    }
}
