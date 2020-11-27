package io.metersphere.api.dto.definition.parse.postman;

import lombok.Data;

import java.util.List;

@Data
public class PostmanItem {
    private String name;
    private PostmanRequest request;
    private List<PostmanItem> item;
}
