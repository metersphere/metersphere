package io.metersphere.dto;

import lombok.Data;

import java.util.List;

@Data
public class GroupPermissionDTO {
    private List<GroupResourceDTO> system;
    private List<GroupResourceDTO> organization;
    private List<GroupResourceDTO> workspace;
    private List<GroupResourceDTO> project;
}
