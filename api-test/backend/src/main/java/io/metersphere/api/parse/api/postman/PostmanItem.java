package io.metersphere.api.parse.api.postman;

import lombok.Data;

import java.util.List;

@Data
public class PostmanItem {
    private String name;
    private PostmanRequest request;
    private List<PostmanItem> item;
}
