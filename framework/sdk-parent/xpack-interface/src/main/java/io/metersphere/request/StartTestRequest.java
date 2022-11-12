package io.metersphere.request;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class StartTestRequest {
    private String image;
    private Map<String, String> env = new HashMap<>();
}
