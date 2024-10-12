package io.metersphere.system.dto.taskhub;

import io.metersphere.system.dto.sdk.OptionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wx
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ResourcePoolOptionsDTO extends OptionDTO {

    @Schema(description = "资源池节点")
    private List<OptionDTO> children = new ArrayList<>();

}
