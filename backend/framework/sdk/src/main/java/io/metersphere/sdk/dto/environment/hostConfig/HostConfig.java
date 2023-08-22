package io.metersphere.sdk.dto.environment.hostConfig;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.hc.core5.net.Host;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class HostConfig implements Serializable {
    @Schema(description = "启用Host")
    private Boolean enable;
    @Schema(description = "Host列表")
    private List<Host> hosts;
    @Serial
    private static final long serialVersionUID = 1L;

}