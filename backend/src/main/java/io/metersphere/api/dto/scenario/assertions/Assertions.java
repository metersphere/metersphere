package io.metersphere.api.dto.scenario.assertions;

import lombok.Data;

import java.util.List;

@Data
public class Assertions {
    private List<AssertionRegex> regex;
    private List<AssertionJsonPath> jsonPath;
    private List<AssertionJSR223> jsr223;
    private List<AssertionXPath2> xpath2;
    private AssertionDuration duration;
}
