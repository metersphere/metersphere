package io.metersphere.api.parser.api.postman;

import lombok.Data;

@Data
public class PostmanCollectionInfo {
    private String postmanId;
    private String name;
    private String schema;
}
