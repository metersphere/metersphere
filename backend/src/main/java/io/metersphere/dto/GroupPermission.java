package io.metersphere.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GroupPermission implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String resourceId;
    private Boolean checked = false;
    private Boolean license = false;
}
