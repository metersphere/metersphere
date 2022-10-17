package io.metersphere.api.parse.postman;


import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.json.JSONObject;

import java.util.List;

@Data
public class PostmanRequest {

    private String method;
    private String schema;
    private List<PostmanKeyValue> header;
    private ObjectNode body;
    private ObjectNode auth;
    private PostmanUrl url;
    private String description;
}
