package io.metersphere.system.controller.param;

import io.metersphere.validation.groups.Created;
import io.metersphere.validation.groups.Updated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PluginUpdateRequestDefinition {
    @NotBlank(message = "{plugin.id.not_blank}", groups = {Updated.class})
    @Size(min = 1, max = 50, message = "{plugin.id.length_range}", groups = {Updated.class})
    private String id;

    @NotBlank(groups = {Created.class})
    @Size(min = 1, max = 255, groups = {Created.class, Updated.class})
    private String name;

    @Size(max = 1000, groups = {Created.class, Updated.class})
    private String description;
}
