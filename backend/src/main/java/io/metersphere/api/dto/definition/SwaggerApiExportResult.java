package io.metersphere.api.dto.definition;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.definition.parse.swagger.SwaggerInfo;
import io.metersphere.api.dto.definition.parse.swagger.SwaggerTag;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SwaggerApiExportResult extends ApiExportResult{
    private String openapi;
    private SwaggerInfo info;
    private JSONObject externalDocs;
    private List<String> servers;
    private List<SwaggerTag> tags;
    private JSONObject paths;   //  Map<String, Object>, Object 里放 Operation 对象
    private JSONObject components;
}
