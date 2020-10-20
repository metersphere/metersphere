package io.metersphere.notice.domain;

import io.metersphere.base.domain.MessageTask;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MessageDetail {
    private List<String> userIds = new ArrayList<>();
    private List<String> events = new ArrayList<>();
    private String taskType;
    private String webhook;
    private String type;
    private String identification;
    private Boolean isSet;
}
