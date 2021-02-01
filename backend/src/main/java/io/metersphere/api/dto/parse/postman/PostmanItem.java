package io.metersphere.api.dto.parse.postman;

import lombok.Data;

import java.util.List;

@Data
public class PostmanItem {
    private String name;
    private List<PostmanEvent> event;
    private PostmanRequest request;
    private List<PostmanItem> item;
}
