package io.metersphere.dto;


import lombok.Data;

import java.util.List;

@Data
public class GroupJson {
    private List<GroupResource> resource;
    private List<GroupPermission> permissions;
}
