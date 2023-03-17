package io.metersphere.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiMockEnvUpdateDTO {
    private String oldBaseUrl;
    private String baseUrl;
    private int limitStart;
    private int limitSize;
}
