package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserHeader implements Serializable {
    private String id;

    private String userId;

    private String props;

    private String type;

    private static final long serialVersionUID = 1L;
}