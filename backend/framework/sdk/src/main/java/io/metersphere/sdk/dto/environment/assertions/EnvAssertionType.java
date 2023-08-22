package io.metersphere.sdk.dto.environment.assertions;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EnvAssertionType {
    public final static String REGEX = "Regex";
    public final static String DURATION = "Duration";
    public final static String JSON_PATH = "JSONPath";
    public final static String JSR223 = "JSR223";
    public final static String TEXT = "Text";
    public final static String XPATH2 = "XPath2";
    @Schema(description = "是否启用")
    private boolean enable = true;

    private String type;
}
