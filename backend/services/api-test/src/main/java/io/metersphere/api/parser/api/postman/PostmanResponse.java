package io.metersphere.api.parser.api.postman;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
public class PostmanResponse {

    private Integer code;
    private String name;
    private String status;
    private List<PostmanKeyValue> header;
    private JsonNode body;
}
