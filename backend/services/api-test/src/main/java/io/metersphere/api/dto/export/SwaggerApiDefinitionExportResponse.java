package io.metersphere.api.dto.export;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

/**
 * @author wx
 */
@Data
public class SwaggerApiDefinitionExportResponse extends ApiDefinitionExportResponse {

    private String openapi;
    private SwaggerInfo info;
    private JsonNode externalDocs;
    private List<String> servers;
    private List<SwaggerTag> tags;
    private JsonNode paths;
    private JsonNode components;
}
