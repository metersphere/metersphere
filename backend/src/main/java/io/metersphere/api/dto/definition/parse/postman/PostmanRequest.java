package io.metersphere.api.dto.definition.parse.postman;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

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
