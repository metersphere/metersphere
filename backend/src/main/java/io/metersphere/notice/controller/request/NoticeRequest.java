package io.metersphere.notice.controller.request;

import io.metersphere.notice.domain.NoticeDetail;
import lombok.Data;

import java.util.List;

@Data
public class NoticeRequest {
    private String testId;
    private List<NoticeDetail> notices;
}
