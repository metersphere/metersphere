package io.metersphere.system.notice.sender;


import io.metersphere.system.notice.MessageDetail;
import io.metersphere.system.notice.NoticeModel;

public interface NoticeSender {
    void send(MessageDetail messageDetail, NoticeModel noticeModel);
}
