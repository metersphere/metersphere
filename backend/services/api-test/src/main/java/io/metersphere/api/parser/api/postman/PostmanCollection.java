package io.metersphere.api.parser.api.postman;

import lombok.Data;

import java.util.List;

@Data
public class PostmanCollection {

    private PostmanCollectionInfo info;
    private List<PostmanItem> item;
    private List<PostmanKeyValue> variable;
}
