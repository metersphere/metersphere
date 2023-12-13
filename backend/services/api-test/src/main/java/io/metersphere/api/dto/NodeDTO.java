package io.metersphere.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class NodeDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 接口测试 性能测试 node节点ip
     */
    private String ip;

    /**
     * 接口测试 性能测试 node节点端口
     */
    private String port;

    /**
     * 资源池最大并发数
     */
    private int podThreads;

}
