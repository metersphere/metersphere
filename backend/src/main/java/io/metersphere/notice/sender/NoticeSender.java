package io.metersphere.notice.sender;

import io.metersphere.notice.domain.MessageDetail;
import org.springframework.scheduling.annotation.Async;

public interface NoticeSender {
    @Async
    void send(MessageDetail messageDetail, NoticeModel noticeModel);
}
