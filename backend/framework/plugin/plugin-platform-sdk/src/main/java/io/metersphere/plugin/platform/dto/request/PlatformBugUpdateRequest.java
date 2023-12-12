package io.metersphere.plugin.platform.dto.request;

import io.metersphere.plugin.platform.dto.reponse.PlatformBugDTO;
import io.metersphere.plugin.platform.dto.reponse.PlatformStatusDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class PlatformBugUpdateRequest extends PlatformBugDTO {

    /**
     * 服务集成配置的用户及平台信息
     */
    private String userPlatformConfig;
    /**
     * 项目配置信息
     */
    private String projectConfig;
    /**
     * 该缺陷关联的附件集合
     */
    private Set<String> msAttachmentNames;
    /**
     * 第三方平台缺陷的状态
     */
    private PlatformStatusDTO transitions;
}
