package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 *用来返回TestResourceBlob的结构
 */
@Getter
@Setter
public class TestResourceReturnDTO {
    /**
     * type为 node时, 性能测试的镜像
     */
    @Schema(description =  "type为node时, 性能测试的镜像")
    private String loadTestImage;

    /**
     * type为 node时, 性能测试jvm配置
     */
    @Schema(description =  "type为node时, 性能测试jvm配置")
    private String loadTestHeap;

    /**
     * type为 node时, 接口测试 性能测试 node 节点配置
     */
    @Schema(description =  "type为node时, 接口测试,性能测试node节点配置")
    private List<TestResourceNodeDTO> nodesList;

    /**
     *  type为 k8s 时，接口测试，性能测试的ip
     */
    @Schema(description =  "type为k8s时，接口测试，性能测试的ip")
    private String ip;
    /**
     *  type为 k8s 时，接口测试，性能测试的token
     */
    @Schema(description =  "type为k8s时，接口测试，性能测试的token")
    private String token;
    /**
     *  type为 k8s 时，接口测试，性能测试的命名空间
     */
    @Schema(description =  "type为k8s时，接口测试，性能测试的命名空间")
    private String namespace;
    /**
     *  type为 k8s 时，接口测试，性能测试，UI测试的最大并发数
     */
    @Schema(description =  "type为k8s时，接口测试，性能测试，UI测试的最大并发数")
    private Integer concurrentNumber;

    /**
     *  type为 k8s 时，接口测试，性能测试的单pod 最大线程数
     */
    @Schema(description =  "type为k8s时，接口测试，性能测试的单pod最大线程数")
    private Integer podThreads;

    /**
     *  type为 k8s 时，性能测试自定义JOB模版 string
     */
    @Schema(description =  " type为k8s时，性能测试自定义JOB模版string")
    private String jobDefinition;

    /**
     *  type为 k8s 时，接口测试deployName
     */
    @Schema(description =  "type为k8s时，接口测试deployName")
    private String deployName;

    /**
     * UI测试的grid配置
     */
    @Schema(description =  "UI测试的grid配置")
    private String uiGrid;

    @Schema(description =  "grid最大线程数")
    private Integer girdConcurrentNumber;

    /**
     * 关联的组织id集合
     */
    @Schema(description =  "关联的组织id和名称的集合")
    private List<OrgIdNameDTO> orgIdNameMap;
}
