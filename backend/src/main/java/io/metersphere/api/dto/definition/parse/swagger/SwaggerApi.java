package io.metersphere.api.dto.definition.parse.swagger;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

@Data
public class SwaggerApi {
    private String swagger;
    private SwaggerInfo info;
    private String host;
    private String basePath;
    private List<String> schemes;
    private List<SwaggerTag> tags;
    private JSONObject paths;
    private JSONObject definitions;
}
