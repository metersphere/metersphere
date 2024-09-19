package io.metersphere.bug.service;

import io.metersphere.bug.domain.Bug;
import io.metersphere.bug.domain.BugExample;
import io.metersphere.bug.dto.BugSyncResult;
import io.metersphere.bug.dto.request.BugSyncRequest;
import io.metersphere.bug.enums.BugPlatform;
import io.metersphere.bug.mapper.BugMapper;
import io.metersphere.project.domain.Project;
import io.metersphere.project.mapper.ProjectMapper;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.project.service.ProjectTemplateService;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.Template;
import io.metersphere.system.domain.TemplateExample;
import io.metersphere.system.mapper.TemplateMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
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
     * 同步全量缺陷
     * @param request 同步全量参数
     * @param currentUser 当前用户
     */
    public void syncAllBugs(BugSyncRequest request, String currentUser, String language, String triggerMode) {
        try {
            // 获取当前项目同步缺陷唯一Key
            String syncValue = bugSyncExtraService.getSyncKey(request.getProjectId());
            if (StringUtils.isEmpty(syncValue)) {
                // 不存在, 设置保证唯一性, 并开始同步
                bugSyncExtraService.setSyncKey(request.getProjectId());
                Project project = getProjectById(request.getProjectId());
                bugService.syncPlatformAllBugs(request, project, currentUser, language, triggerMode);
            }
        } catch (Exception e) {
            bugSyncExtraService.deleteSyncKey(request.getProjectId());
            throw new MSException(e.getMessage());
        }
    }

    /**
     * 同步缺陷
     * @param projectId 项目ID
     */
    public void syncBugs(String projectId, String currentUser, String language, String triggerMode) {
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
                    bugService.syncPlatformBugs(bugs, project, currentUser, language, triggerMode);
                }
            }
        } catch (Exception e) {
            bugSyncExtraService.deleteSyncKey(projectId);
            throw new MSException(e);
        }
    }

    /**
     * 校验同步状态
     * @param projectId 项目ID
     * @return 同步结果
     */
    public BugSyncResult checkSyncStatus(String projectId) {
        BugSyncResult result = BugSyncResult.builder().complete(Boolean.FALSE).msg(StringUtils.EMPTY).build();
        String syncValue = bugSyncExtraService.getSyncKey(projectId);
        // redis-key 存在, 说明正在同步
        if (StringUtils.isNotEmpty(syncValue)) {
            return result;
        }
        // 否则, 项目同步已结束, 设置同步Msg
        result.setComplete(Boolean.TRUE);
        result.setMsg(bugSyncExtraService.getSyncErrorMsg(projectId));
        if (StringUtils.isNotEmpty(result.getMsg())) {
            // 清空同步异常信息
            bugSyncExtraService.deleteSyncErrorMsg(projectId);
        }
        return result;
    }

    /**
     * 定时任务同步缺陷(存量-默认中文环境通知)
     * @param projectId 项目ID
     * @param scheduleUser 任务触发用户
     */
    public void syncPlatformBugBySchedule(String projectId, String scheduleUser) {
        syncBugs(projectId, scheduleUser, Locale.SIMPLIFIED_CHINESE.getLanguage(), Translator.get("sync_mode.auto"));
    }

    /**
     * 定时任务同步缺陷(全量-默认中文环境通知)
     * @param projectId 项目ID
     * @param scheduleUser 任务触发用户
     */
    public void syncPlatformAllBugBySchedule(String projectId, String scheduleUser) {
        BugSyncRequest syncRequest = new BugSyncRequest();
        syncRequest.setProjectId(projectId);
        syncAllBugs(syncRequest, scheduleUser, Locale.SIMPLIFIED_CHINESE.getLanguage(), Translator.get("sync_mode.auto"));
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
