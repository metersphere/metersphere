package io.metersphere.plugin.platform.dto.request;

import lombok.Data;

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
     * 开始页码
     */
    private int startPage;

    /**
     * 每页条数
     */
    private int pageSize;
}
