package io.metersphere.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupResourceDTO {
    private GroupResource resource;
    private List<GroupPermission> permissions;
}
