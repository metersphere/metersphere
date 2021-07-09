package io.metersphere.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GroupPermissionDTO {
    private List<GroupResourceDTO> permissions = new ArrayList<>();
}
