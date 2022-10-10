package io.metersphere.dto;


import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GroupJson implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<GroupResource> resource;
    private List<GroupPermission> permissions;
}
