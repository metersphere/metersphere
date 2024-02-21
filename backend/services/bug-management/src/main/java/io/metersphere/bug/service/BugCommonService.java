package io.metersphere.bug.service;

import io.metersphere.bug.dto.BugTemplateInjectField;
import io.metersphere.bug.enums.BugPlatform;
import io.metersphere.bug.enums.BugTemplateCustomField;
import io.metersphere.plugin.platform.dto.SelectOption;
import io.metersphere.plugin.platform.dto.request.GetOptionRequest;
import io.metersphere.plugin.platform.spi.AbstractPlatformPlugin;
import io.metersphere.plugin.platform.spi.Platform;
import io.metersphere.project.dto.ProjectUserDTO;
import io.metersphere.project.request.ProjectMemberRequest;
import io.metersphere.project.service.ProjectApplicationService;
import io.metersphere.project.service.ProjectMemberService;
import io.metersphere.sdk.util.JSON;
import io.metersphere.system.domain.ServiceIntegration;
import io.metersphere.system.service.PlatformPluginService;
import io.metersphere.system.service.PluginLoadService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class BugCommonService {

	@Resource
	private PluginLoadService pluginLoadService;
	@Resource
	private ProjectMemberService projectMemberService;
	@Resource
	private PlatformPluginService platformPluginService;
	@Resource
	private ProjectApplicationService projectApplicationService;

	/**
	 * 获取表头处理人选项
	 * @param projectId 项目ID
	 * @return 处理人选项集合
	 */
	public List<SelectOption> getHeaderHandlerOption(String projectId) {
		String platformName = projectApplicationService.getPlatformName(projectId);
		// 需要校验服务集成是否开启
		ServiceIntegration serviceIntegration = projectApplicationService.getPlatformServiceIntegrationWithSyncOrDemand(projectId, true);
		if (StringUtils.equals(platformName, BugPlatform.LOCAL.getName())) {
			// Local处理人
			return getLocalHandlerOption(projectId);
		} else {
			// 第三方平台处理人
			// 获取插件中自定义的注入字段(处理人)
			Platform platform = platformPluginService.getPlatform(serviceIntegration.getPluginId(), serviceIntegration.getOrganizationId(),
					new String(serviceIntegration.getConfiguration()));
			List<SelectOption> platformHandlerOption = new ArrayList<>();
			List<BugTemplateInjectField> platformInjectFields = getPlatformInjectFields(projectId);
			for (BugTemplateInjectField injectField : platformInjectFields) {
				if (StringUtils.equals(injectField.getKey(), BugTemplateCustomField.HANDLE_USER.getId())) {
					GetOptionRequest request = new GetOptionRequest();
					request.setOptionMethod(injectField.getOptionMethod());
					request.setProjectConfig(projectApplicationService.getProjectBugThirdPartConfig(projectId));
					platformHandlerOption = platform.getFormOptions(request);
				}
			}
			return platformHandlerOption;
		}
	}

	/**
	 * 项目成员选项(处理人)
	 * @param projectId 项目ID
	 * @return 处理人选项集合
	 */
	public List<SelectOption> getLocalHandlerOption(String projectId) {
		ProjectMemberRequest request = new ProjectMemberRequest();
		request.setProjectId(projectId);
		List<ProjectUserDTO> projectMembers = projectMemberService.listMember(request);
		return projectMembers.stream().map(user -> {
			SelectOption option = new SelectOption();
			option.setText(user.getName());
			option.setValue(user.getId());
			return option;
		}).toList();
	}

	/**
	 * 获取平台注入的字段
	 * @param projectId 项目ID
	 * @return 注入的字段集合
	 */
	public List<BugTemplateInjectField> getPlatformInjectFields(String projectId) {
		// 获取插件中自定义的注入字段(处理人)
		ServiceIntegration serviceIntegration = projectApplicationService.getPlatformServiceIntegrationWithSyncOrDemand(projectId, true);
		AbstractPlatformPlugin platformPlugin = (AbstractPlatformPlugin) pluginLoadService.getMsPluginManager().getPlugin(serviceIntegration.getPluginId()).getPlugin();
		Object scriptContent = pluginLoadService.getPluginScriptContent(serviceIntegration.getPluginId(), platformPlugin.getProjectBugTemplateInjectField());
		String injectFields = JSON.toJSONString(((Map<?, ?>) scriptContent).get("injectFields"));
		return JSON.parseArray(injectFields, BugTemplateInjectField.class);
	}
}
