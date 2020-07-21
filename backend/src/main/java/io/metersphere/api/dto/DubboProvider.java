package io.metersphere.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class DubboProvider {
    private String version;
    private String service;
    private String serviceInterface;
    private List<String> methods;
}
