package io.metersphere.api.dto.debug;

import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.swagger.v3.oas.annotations.media.Schema;
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
     * 资源类型
     * @see io.metersphere.api.constants.ApiResourceType
     */
    private String resourceType;
    /**
     * 执行组件
     */
    private AbstractMsTestElement testElement;
    /**
     * 是否是本地执行
     */
    private Boolean frontendDebug = false;
    /**
     * 新上传的文件ID
     * 创建时先按ID创建目录，再把文件放入目录
     */
    @Schema(description = "新上传的文件ID")
    private List<String> uploadFileIds;
    /**
     * 新关联的文件ID
     */
    @Schema(description = "关联文件ID")
    private List<String> linkFileIds;
}
