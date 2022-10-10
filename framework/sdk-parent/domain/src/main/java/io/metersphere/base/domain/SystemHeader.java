package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class SystemHeader implements Serializable {
    private String type;

    private String props;

    private static final long serialVersionUID = 1L;
}