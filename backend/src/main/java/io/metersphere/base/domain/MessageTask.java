package io.metersphere.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class MessageTask implements Serializable {
    private String id;

    private String type;

    private String event;

    private String userId;

    private String taskType;

    private String webhook;

    private String identification;

    private Boolean isSet;

    private String testId;

    private Long createTime;

    private String workspaceId;

    private String template;

    private static final long serialVersionUID = 1L;
}