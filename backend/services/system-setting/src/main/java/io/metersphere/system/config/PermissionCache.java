package io.metersphere.system.config;

import io.metersphere.system.dto.permission.PermissionDefinitionItem;
import lombok.Data;

import java.util.List;

@Data
public class PermissionCache {
    private List<PermissionDefinitionItem> permissionDefinition;
}
