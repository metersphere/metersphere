package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoadTestFollow implements Serializable {
    private String testId;

    private String followId;

    private static final long serialVersionUID = 1L;
}