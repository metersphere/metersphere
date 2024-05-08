package io.metersphere.plugin.platform.dto.request;

import io.metersphere.plugin.platform.dto.response.PlatformBugDTO;
import io.metersphere.plugin.platform.dto.response.PlatformStatusDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
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
    /**
     * MS平台缺陷富文本文件集合
     */
    private Map<String, File> richFileMap = new HashMap<>();
    /**
     * 当前MS站点URL
     */
    private String baseUrl;
}
