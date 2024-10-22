package io.metersphere.system.dto.taskhub;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author wx
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class ResourcePoolStatusDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "详情的id")
    private String id;

    @Schema(description = "状态 (true 正常 false 异常)")
    private boolean status;

}
