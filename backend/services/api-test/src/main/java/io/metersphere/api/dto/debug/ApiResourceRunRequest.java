package io.metersphere.api.dto.debug;

import lombok.Data;

import java.util.List;

@Data
public class ApiResourceRunRequest {
    private String id;
    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 资源ID
     */
    private String testId;
    /**
     * 测试报告ID
     */
    private String reportId;
    /**
     * 环境ID
     */
    private String environmentId;
    /**
     * 执行模式
     */
    private String runMode;
    /**
     * 资源类型
     * @see io.metersphere.api.constants.ApiResourceType
     */
    private String resourceType;
    /**
     * 请求内容
     */
    private String request;
    /**
     * 点击调试时尚未保存的文件列表
     */
    private List<String> tempFileIds;
}
