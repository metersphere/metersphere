package io.metersphere.api.parse.postman;

import lombok.Data;

@Data
public class PostmanKeyValue {
    private String key;
    private String value;
    private String type;
    private String description;
    private String contentType;
    private boolean required;

    public PostmanKeyValue() {
    }

    public PostmanKeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
