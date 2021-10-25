package io.metersphere.notice.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class MessageDetail implements Serializable {
    private List<String> userIds = new ArrayList<>();
    private String id;
    private String event;
    private String taskType;
    private String webhook;
    private String type;
    private String identification;
    private Boolean isSet;
    private String testId;
    private Long createTime;
    private String template;
}
