//package io.metersphere.bug.service;
//
//import io.metersphere.bug.dto.BugStatusOptionDTO;
//import io.metersphere.bug.enums.BugPlatform;
//import io.metersphere.plugin.platform.spi.Platform;
//import io.metersphere.project.service.ProjectApplicationService;
//import io.metersphere.project.service.ProjectTemplateService;
//import io.metersphere.sdk.exception.MSException;
//import io.metersphere.sdk.util.Translator;
//import io.metersphere.system.domain.ServiceIntegration;
//import io.metersphere.system.service.PlatformPluginService;
//import jakarta.annotation.Resource;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//@Transactional(rollbackFor = Exception.class)
//public class BugStatusService {
//
//    @Resource
//    private ProjectApplicationService projectApplicationService;
//    @Resource
//    private ProjectTemplateService projectTemplateService;
//    @Resource
//    private PlatformPluginService platformPluginService;
//
//    /**
//     * 获取状态下拉选项
//     * @param projectId 项目ID
//     * @return 选项集合
//     */
//    public List<BugStatusOptionDTO> getProjectStatusOption(String projectId) {
//        // TODO: 获取状态下拉选项 {Local平台: 直接取状态流中的选项, 第三方平台: 取第三方插件平台的状态和状态流中的选项}
//        String platformName = projectApplicationService.getPlatformName(projectId);
//        if (StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
//            return getProjectStatusItemOption(projectId);
//        } else {
//            // 状态流 && 第三方平台状态流
//            ServiceIntegration serviceIntegration = projectTemplateService.getServiceIntegration(projectId);
//            if (serviceIntegration == null) {
//                // 项目未配置第三方平台
//                throw new MSException(Translator.get("third_party_not_config"));
//            }
//            // 获取配置平台, 获取第三方平台状态流
//            Platform platform = platformPluginService.getPlatform(serviceIntegration.getPluginId(), serviceIntegration.getOrganizationId(),
//                    new String(serviceIntegration.getConfiguration()));
////            List<PlatformStatusDTO> statusList = platform.getStatusList();
//            // 获取项目状态流
////            List<BugStatusOptionDTO> projectStatusItemOption = getProjectStatusItemOption(projectId);
//        }
//        return null;
//    }
//
//    /**
//     * 获取项目状态流选项
//     * @param projectId 项目ID
//     * @return 选项集合
//     */
//    public List<BugStatusOptionDTO> getProjectStatusItemOption(String projectId) {
//        // TODO: 获取项目状态流选项
//        return null;
//    }
//}
