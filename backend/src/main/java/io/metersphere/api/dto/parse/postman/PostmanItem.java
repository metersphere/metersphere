package io.metersphere.api.dto.parse.postman;

import lombok.Data;

@Data
public class PostmanItem {

    private String name;
    private PostmanRequest request;
}
