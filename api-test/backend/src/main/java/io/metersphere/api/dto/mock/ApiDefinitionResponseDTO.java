package io.metersphere.api.dto.mock;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ApiDefinitionResponseDTO {
    private String returnData;
    private int returnCode = 404;
    private Map<String,String> headers = new HashMap<>();
}
