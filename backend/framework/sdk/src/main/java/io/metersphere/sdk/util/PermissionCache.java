package io.metersphere.sdk.util;

import io.metersphere.sdk.dto.PermissionDefinitionItem;
import lombok.Data;

import java.util.List;

@Data
public class PermissionCache {
    private List<PermissionDefinitionItem> permissionDefinition;
}
