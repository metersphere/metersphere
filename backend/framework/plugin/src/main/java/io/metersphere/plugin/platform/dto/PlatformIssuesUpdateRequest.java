package io.metersphere.plugin.platform.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class PlatformIssuesUpdateRequest extends PlatformIssuesDTO {

    /**
     * 用户信息的第三方平台的配置项
     */
    private String userPlatformUserConfig;
    /**
     * 项目设置的配置项
     */
    private String projectConfig;
    /**
     * 改缺陷关联的附件集合
     */
    private Set<String> msAttachmentNames;
    /**
     * 第三方平台缺陷的状态
     */
    private PlatformStatusDTO transitions;
}
