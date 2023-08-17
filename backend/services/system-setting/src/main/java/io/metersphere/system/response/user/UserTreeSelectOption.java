package io.metersphere.system.response.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserTreeSelectOption extends UserSelectOption {
    @Schema(description = "父节点ID")
    private String parentId;

    public UserTreeSelectOption(String id, String name, String parentId) {
        this.setId(id);
        this.setName(name);
        this.setParentId(parentId);
    }
}
