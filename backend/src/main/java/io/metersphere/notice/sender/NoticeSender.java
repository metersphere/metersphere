package io.metersphere.notice.sender;

import io.metersphere.notice.domain.MessageDetail;

public interface NoticeSender {
    void send(MessageDetail messageDetail, NoticeModel noticeModel);
}
