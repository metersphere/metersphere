package io.metersphere.api.dto.definition.parse.swagger;

import lombok.Data;

import java.util.List;

@Data
public class SwaggerRequest {
    private List<String> tags;
    private String summary;
    private String description;
    private String operationId;
    private List<String> consumes;
    private List<String> produces;
    private List<SwaggerParameter> parameters;
}
