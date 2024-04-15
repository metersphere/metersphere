package io.metersphere.plugin.platform.dto.request;

import io.metersphere.plugin.platform.dto.response.PlatformBugDTO;
import lombok.Data;

import java.util.List;

@Data
public class SyncBugRequest {
    /**
     * 项目设置的配置项
     */
    private String projectConfig;

    /**
     * 需要同步的平台缺陷列表
     */
    private List<PlatformBugDTO> bugs;
}
