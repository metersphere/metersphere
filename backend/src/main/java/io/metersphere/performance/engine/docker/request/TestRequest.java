package io.metersphere.performance.engine.docker.request;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class TestRequest extends BaseRequest {
    private int size;
    private String fileString;
    private String image;
    private Map<String, String> testData = new HashMap<>();
    private Map<String, String> env = new HashMap<>();
    private Map<String, byte[]> testJars = new HashMap<>();
}
