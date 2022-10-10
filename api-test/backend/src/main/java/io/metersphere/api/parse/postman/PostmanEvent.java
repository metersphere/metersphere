package io.metersphere.api.parse.postman;

import lombok.Data;

@Data
public class PostmanEvent {
    private String listen;
    private PostmanScript script;
}
