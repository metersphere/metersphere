package io.metersphere.plugin.platform.dto.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DemandPageRequest {

    /**
     * 项目配置信息
     */
    private String projectConfig;

    /**
     * 需求分页查询关键字
     */
    private String query;

    /**
     * 筛选条件
     */
    private Map<String, List<String>> filter;

    /**
     * 开始页码
     */
    private int startPage;

    /**
     * 每页条数
     */
    private int pageSize;
}
