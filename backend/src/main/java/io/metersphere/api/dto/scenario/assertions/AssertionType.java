package io.metersphere.api.dto.scenario.assertions;

import lombok.Data;

@Data
public class AssertionType {
    public final static String REGEX = "Regex";
    public final static String DURATION = "Duration";
    public final static String JSON_PATH = "JSONPath";
    public final static String JSR223 = "JSR223";
    public final static String TEXT = "Text";
    public final static String XPATH2 = "XPath2";

    private String type;
}
