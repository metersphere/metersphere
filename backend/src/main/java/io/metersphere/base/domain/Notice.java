package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class Notice implements Serializable {
    private String id;

    private String event;

    private String testId;

    private String name;

    private String enable;

    private static final long serialVersionUID = 1L;
}