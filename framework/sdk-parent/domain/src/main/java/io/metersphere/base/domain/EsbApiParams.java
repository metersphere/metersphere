package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class EsbApiParams implements Serializable {
    private String id;

    private String resourceId;

    private static final long serialVersionUID = 1L;
}