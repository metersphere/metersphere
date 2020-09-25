package io.metersphere.notice.controller.request;

import io.metersphere.base.domain.NoticeDetail;
import lombok.Data;

import java.util.List;

@Data
public class NoticeRequest extends NoticeDetail {
   private String testId;
   private List<NoticeDetail> notices;

}
