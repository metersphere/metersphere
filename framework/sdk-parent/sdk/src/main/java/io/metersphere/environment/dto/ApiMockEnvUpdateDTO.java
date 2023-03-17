package io.metersphere.environment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiMockEnvUpdateDTO {
    private String baseUrl;
    private int limitStart;
    private int limitSize;
}
