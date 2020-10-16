package io.metersphere.notice.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MessageDetail {
    private List<String> userIds = new ArrayList<>();
    private List<String> userNames = new ArrayList<>();
    private List<String> events = new ArrayList<>();
    private String id;
    private String type;
    private String taskType;


}
