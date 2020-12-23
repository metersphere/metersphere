package io.metersphere.api.dto.scenario.controller;

import lombok.Data;

@Data
public class IfController {
    private String type;
    private String id;
    private boolean enable = true;
    private String variable;
    private String operator;
    private String value;
}
