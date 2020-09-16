package io.metersphere.notice.controller.request;

import io.metersphere.base.domain.Notice;
import lombok.Data;

import java.util.List;

@Data
public class NoticeRequest extends Notice {
   private String testId;
   private List<Notice> notices;
}
