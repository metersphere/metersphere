package io.metersphere.notice.controller.request;

import io.metersphere.base.domain.Notice;
import lombok.Data;

@Data
public class NoticeRequest extends Notice {
    private String testId;
}
