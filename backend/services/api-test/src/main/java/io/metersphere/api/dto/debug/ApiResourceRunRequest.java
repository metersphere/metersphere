package io.metersphere.api.dto.debug;

import io.metersphere.plugin.api.spi.AbstractMsTestElement;
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
     * 是否为环境组
     */
    private Boolean grouped = false;
    /**
     * 环境或者环境组ID
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
     * 执行组件
     */
    private AbstractMsTestElement testElement;
    /**
     * 点击调试时尚未保存的文件列表
     */
    private List<String> tempFileIds;
}
