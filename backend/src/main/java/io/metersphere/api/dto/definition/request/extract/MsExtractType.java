package io.metersphere.api.dto.definition.request.extract;

import lombok.Data;

@Data
public class MsExtractType {
    public final static String REGEX = "Regex";
    public final static String JSON_PATH = "JSONPath";
    public final static String XPATH = "XPath";

    private String type;
}
