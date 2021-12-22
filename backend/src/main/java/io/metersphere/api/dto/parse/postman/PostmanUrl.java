package io.metersphere.api.dto.parse.postman;

import lombok.Data;

import java.util.List;

@Data
public class PostmanUrl {

    private String raw;
    private String protocol;
    private String port;
    private List<PostmanKeyValue> query;
    private List<PostmanKeyValue> variable;
}
