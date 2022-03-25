package io.metersphere.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NodeDTO {
    private String ip;
    private boolean enable;
    private Integer port;
    private Integer monitorPort;
    private Integer maxConcurrency;
}
