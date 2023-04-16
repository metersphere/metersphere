package io.metersphere.plugin.platform.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SyncIssuesRequest {
    /**
     * 项目设置的配置项
     */
    private String projectConfig;
    /**
     * 缺陷模板所关联的自定义字段
     */
    private String defaultCustomFields;
    /**
     * 需要同步的缺陷列表
     */
    private List<PlatformIssuesDTO> issues;
}
