package io.metersphere.sdk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestResourceNodeDTO {

    /**
     * 接口测试 性能测试 node节点ip
     */
    @Schema(title = "接口测试性能测试 node节点ip")
    private String ip;

    /**
     * 接口测试 性能测试 node节点端口
     */
    @Schema(title = "接口测试性能测试 node节点端口")
    private String port;

    /**
     * 性能测试 node节点监控器
     */
    @Schema(title = "性能测试 node节点监控器")
    private String monitor;

    /**
     * 接口测试 性能测试 最大并发数
     */
    @Schema(title = "接口测试,性能测试最大并发数")
    private Integer concurrentNumber;

}
