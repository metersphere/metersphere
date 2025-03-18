package io.metersphere.functional.request;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private String user;
    private String text;
    private boolean self;
    private String type;
}
