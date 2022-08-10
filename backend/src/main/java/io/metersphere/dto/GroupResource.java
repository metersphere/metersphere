package io.metersphere.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GroupResource implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private Boolean license = false;
}
