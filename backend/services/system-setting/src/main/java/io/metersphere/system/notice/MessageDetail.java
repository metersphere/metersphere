package io.metersphere.system.notice;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class MessageDetail implements Serializable {
    private List<String> receiverIds = new ArrayList<>();
    private String id;
    private String event;
    private String taskType;
    private String webhook;
    private String type;
    private String dingType;
    private String testId;
    private Long createTime;
    private String subject;
    private String template;
    private String appKey;
    private String appSecret;
    private String projectId;

}
