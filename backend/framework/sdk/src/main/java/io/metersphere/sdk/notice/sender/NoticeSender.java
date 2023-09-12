package io.metersphere.sdk.notice.sender;


import io.metersphere.sdk.notice.MessageDetail;
import io.metersphere.sdk.notice.NoticeModel;

public interface NoticeSender {
    void send(MessageDetail messageDetail, NoticeModel noticeModel);
}
