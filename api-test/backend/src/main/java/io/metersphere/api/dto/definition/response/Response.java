package io.metersphere.api.dto.definition.response;

import lombok.Data;

@Data
public abstract class Response {
    private String id;
    private String name;
    private Boolean enable;
}
