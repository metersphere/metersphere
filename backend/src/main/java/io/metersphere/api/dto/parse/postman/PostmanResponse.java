package io.metersphere.api.dto.parse.postman;

import lombok.Data;

import java.util.List;

@Data
public class PostmanResponse {

    private Integer code;
    private Integer responseTime;
    private String name;
    private PostmanRequest originalRequest;
    private String status;
    private List<PostmanKeyValue> header;
    private String body;
    private String jsonSchema;
    private PostmanUrl url;
    private String description;
}
