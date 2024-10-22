package io.metersphere.system.dto.pool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 用来解析TestResourceBlob的结构
 */
@Getter
@Setter
public class TestResourceDTO {
    /**
     * 资源id
     */
    private String id;

    /**
     * type为 node时, 接口测试 node 节点配置
     */
    @Schema(description = "type为node时, 接口测试")
    private List<TestResourceNodeDTO> nodesList;

    /**
     * type为 k8s 时，接口测试
     */
    @Schema(description = "type为k8s时，接口测试")
    private String ip;
    /**
     * type为 k8s 时，接口测试
     */
    @Schema(description = "type为k8s时，接口测试")
    private String token;
    /**
     * type为 k8s 时，接口测试命名空间
     */
    @Schema(description = "type为k8s时，接口测试")
    private String namespace;
    /**
     * type为 k8s 时，接口测试
     */
    @Schema(description = "type为k8s时，接口测试")
    private Integer concurrentNumber;

    /**
     * type为k8s时，最大任务并发数
     */
    @Schema(description = "type为k8s时，最大任务并发数")
    private Integer podThreads;

    /**
     * type为 k8s 时，接口测试deployName
     */
    @Schema(description = "type为k8s时，接口测试deployName")
    private String deployName;

    @Schema(description = "grid最大线程数")
    private Integer girdConcurrentNumber;

    /**
     * 关联的组织id集合
     */
    @Schema(description = "关联的组织id集合")
    private List<String> orgIds;


}
