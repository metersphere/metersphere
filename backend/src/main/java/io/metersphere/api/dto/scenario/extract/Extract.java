package io.metersphere.api.dto.scenario.extract;

import lombok.Data;

import java.util.List;

@Data
public class Extract {
    private List<ExtractRegex> regex;
    private List<ExtractJSONPath> json;
    private List<ExtractXPath> xpath;
}
