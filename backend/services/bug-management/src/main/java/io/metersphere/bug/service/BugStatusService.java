package io.metersphere.bug.service;

import io.metersphere.bug.enums.BugPlatform;
import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.system.service.BaseStatusFlowSettingService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugStatusService {

   @Resource
   private ProjectApplicationService projectApplicationService;
   @Resource
   private BaseStatusFlowSettingService baseStatusFlowSettingService;

    /**
     * 获取表头缺陷状态选项
     * @param projectId 项目ID
     * @return 选项集合
     */
    public List<SelectOption> getHeaderStatusOption(String projectId) {
        String platformName = projectApplicationService.getPlatformName(projectId);
        if (StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
            // Local状态流
            return getAllLocalStatusOptions(projectId);
        } else {
            // 第三方平台(Local状态流 && 第三方平台状态流)
            List<SelectOption> localStatusOption = getAllLocalStatusOptions(projectId);
            Platform platform = projectApplicationService.getPlatform(projectId, true);
            String projectConfig = projectApplicationService.getProjectBugThirdPartConfig(projectId);
            List<SelectOption> platformStatusOption = platform.getStatusTransitions(projectConfig, null);
            return ListUtils.union(localStatusOption, platformStatusOption);
        }
    }

    /**
     * 获取缺陷下一批状态流转选项
     * @param projectId 项目ID
     * @param fromStatusId 当前状态选项值ID
     * @param platformBugKey 平台缺陷Key
     * @return 选项集合
     */
   public List<SelectOption> getToStatusItemOption(String projectId, String fromStatusId, String platformBugKey) {
       String platformName = projectApplicationService.getPlatformName(projectId);
       if (StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
           // Local状态流
           return getToStatusItemOptionOnLocal(projectId, fromStatusId);
       } else {
           // 第三方平台状态流
           // 获取配置平台, 获取第三方平台状态流
           Platform platform = projectApplicationService.getPlatform(projectId, true);
           String projectConfig = projectApplicationService.getProjectBugThirdPartConfig(projectId);
           return platform.getStatusTransitions(projectConfig, platformBugKey);
       }
   }

   /**
    * 获取缺陷下一批状态流转选项
    * @param projectId 项目ID
    * @param fromStatusId 当前状态选项值ID
    * @return 选项集合
    */
   public List<SelectOption> getToStatusItemOptionOnLocal(String projectId, String fromStatusId) {
       return baseStatusFlowSettingService.getStatusTransitions(projectId, TemplateScene.BUG.name(), fromStatusId);
   }

    /**
     * 获取Local状态流选项
     * @param projectId 项目ID
     * @return 状态流选项
     */
   public List<SelectOption> getAllLocalStatusOptions(String projectId) {
       return baseStatusFlowSettingService.getAllStatusOption(projectId, TemplateScene.BUG.name());
   }
}
