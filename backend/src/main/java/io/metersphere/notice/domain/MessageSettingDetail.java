package io.metersphere.notice.domain;

import lombok.Data;

import java.util.List;

@Data
public class MessageSettingDetail {
    private List<MessageDetail> jenkinsTask;
    private List<MessageDetail> testCasePlanTask;
    private List<MessageDetail> reviewTask;
    private List<MessageDetail> defectTask;
}
