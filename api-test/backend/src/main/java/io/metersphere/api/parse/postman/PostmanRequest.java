package io.metersphere.api.parse.postman;


import lombok.Data;
import org.json.JSONObject;

import java.util.List;

@Data
public class PostmanRequest {

    private String method;
    private String schema;
    private List<PostmanKeyValue> header;
    private JSONObject body;
    private JSONObject auth;
    private PostmanUrl url;
    private String description;
}
