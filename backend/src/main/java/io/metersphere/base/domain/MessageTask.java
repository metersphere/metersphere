package io.metersphere.base.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessageTask implements Serializable {
    private String id;

    private String type;

    private String event;

    private String userid;

    private String username;

    private String tasktype;

    private String webhook;

    private static final long serialVersionUID = 1L;
}