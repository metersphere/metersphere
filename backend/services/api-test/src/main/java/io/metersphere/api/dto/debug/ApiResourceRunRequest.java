package io.metersphere.api.dto.debug;

import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.*;

@Data
public class ApiResourceRunRequest {
    /**
     * 执行组件
     */
    private AbstractMsTestElement testElement;
    /**
     * 新上传的文件ID
     * 创建时先按ID创建目录，再把文件放入目录
     */
    @Schema(description = "新上传的文件ID")
    private List<String> uploadFileIds = new ArrayList<>(0);
    /**
     * 新关联的文件ID
     */
    @Schema(description = "关联文件ID")
    private List<String> linkFileIds = new ArrayList<>(0);
    /**
     * 执行时包含文件的资源 ID 列表
     * 场景执行时，包含 用例、场景、步骤ID
     */
    private Set<String> fileResourceIds = HashSet.newHashSet(0);
    /**
     * 包含文件文件的步骤ID和场景ID的映射
     */
    private Map<String, String> fileStepScenarioMap = new HashMap<>(0);
    /**
     * 执行的资源所属项目的ID列表
     * 场景执行时，为引用的资源的项目ID列表
     */
    private Set<String> refProjectIds = HashSet.newHashSet(0);
}
