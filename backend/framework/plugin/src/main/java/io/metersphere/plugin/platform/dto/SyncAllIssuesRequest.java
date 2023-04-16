package io.metersphere.plugin.platform.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Setter
@Getter
public class SyncAllIssuesRequest extends SyncIssuesRequest {
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

    private boolean pre;

    private Long createTime;

    private Consumer<Map> handleSyncFunc;
}
