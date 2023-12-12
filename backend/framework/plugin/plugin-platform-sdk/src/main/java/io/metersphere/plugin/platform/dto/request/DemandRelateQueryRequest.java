package io.metersphere.plugin.platform.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class DemandRelateQueryRequest {

    /**
     * 项目配置信息
     */
    private String projectConfig;

    /**
     * 关联的需求ID集合
     */
    private List<String> relateDemandIds;
}
