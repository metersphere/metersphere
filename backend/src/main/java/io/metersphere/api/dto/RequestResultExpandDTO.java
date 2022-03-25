package io.metersphere.api.dto;

import io.metersphere.dto.RequestResult;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RequestResultExpandDTO extends RequestResult {
    private String status;
    private Map<String,String> attachInfoMap;
}
