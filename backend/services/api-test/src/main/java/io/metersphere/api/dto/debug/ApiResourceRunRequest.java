package io.metersphere.api.dto.debug;

import io.metersphere.plugin.api.spi.AbstractMsTestElement;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * 执行的资源ID列表
     * 场景执行时，为关联的所有用例和场景列表
     */
    private Set<String> refResourceIds = HashSet.newHashSet(0);
    /**
     * 执行的资源所属项目的ID列表
     * 场景执行时，为引用的资源的项目ID列表
     */
    private Set<String> refProjectIds = HashSet.newHashSet(0);
}
