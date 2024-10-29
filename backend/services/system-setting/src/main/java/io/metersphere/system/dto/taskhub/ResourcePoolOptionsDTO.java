package io.metersphere.system.dto.taskhub;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ResourcePoolOptionsDTO extends OptionDTO {

    @Schema(description = "资源池节点")
    private List<OptionDTO> children = new ArrayList<>();

}
