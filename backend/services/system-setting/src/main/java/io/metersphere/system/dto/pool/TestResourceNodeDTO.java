package io.metersphere.system.dto.pool;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TestResourceNodeDTO {

    /**
     * 接口测试 性能测试 node节点ip
     */
    @Schema(description =  "接口测试性能测试 node节点ip")
    private String ip;

    /**
     * 接口测试 性能测试 node节点端口
     */
    @Schema(description =  "接口测试性能测试 node节点端口")
    private String port;

    /**
     * 接口测试 性能测试 最大并发数
     */
    @Schema(description =  "接口测试,性能测试最大并发数")
    private Integer concurrentNumber;

    /**
     * 接口测试 性能测试 最大并发数
     */
    @Schema(description =  "接口测试,性能测试单个任务最大并发数，有可能为null，为null则置为3")
    private Integer singleTaskConcurrentNumber = 3;

}
