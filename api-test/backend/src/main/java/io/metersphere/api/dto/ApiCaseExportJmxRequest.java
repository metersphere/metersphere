package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiCaseExportJmxRequest {
    private List<String> caseIds;
    private String envId;
}
