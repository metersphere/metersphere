package io.metersphere.api.dto.parse.postman;

import lombok.Data;

@Data
public class PostmanKeyValue {
    private String key;
    private String value;
    private String type;
    private String description;
    private String contentType;

    public PostmanKeyValue() {
    }

    public PostmanKeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
