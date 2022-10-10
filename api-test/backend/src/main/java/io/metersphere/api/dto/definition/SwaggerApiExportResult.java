package io.metersphere.api.dto.definition;


import com.fasterxml.jackson.databind.JsonNode;
import io.metersphere.api.parse.api.swagger.SwaggerInfo;
import io.metersphere.api.parse.api.swagger.SwaggerTag;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SwaggerApiExportResult extends ApiExportResult{
    private String openapi;
    private SwaggerInfo info;
    private JsonNode externalDocs;
    private List<String> servers;
    private List<SwaggerTag> tags;
    private JsonNode paths;   //  Map<String, Object>, Object 里放 Operation 对象
    private JsonNode components;
}
