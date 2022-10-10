package io.metersphere.notice.controller.request;

import io.metersphere.notice.domain.MessageDetail;
import lombok.Data;

import java.util.List;

@Data
public class MessageRequest {
    private List<MessageDetail> messageDetail;
}
