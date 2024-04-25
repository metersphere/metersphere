package io.metersphere.api.parser.api.postman;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
public class PostmanRequest {

    private String method;
    private String schema;
    private List<PostmanKeyValue> header;
    private JsonNode body;
    private JsonNode auth;
    private JsonNode url;
}
