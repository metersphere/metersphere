package io.metersphere.plugin.platform.dto.request;

import lombok.Data;

@Data
public class PlatformBugDeleteRequest {

	/**
	 * 项目配置信息 (兼容部分平台)
	 */
	private String projectConfig;

	/**
	 * 平台缺陷Key
	 */
	private String platformBugKey;
}
