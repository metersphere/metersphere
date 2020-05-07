package io.metersphere.api.jmeter;

import lombok.Data;

@Data
public class RequestResult {

    private String name;

    private String url;

    private long requestSize;

    private boolean success;

    private String headers;

    private String cookies;

    private String body;

    private int totalAssertions = 0;

    private int passAssertions = 0;

    private final ResponseResult responseResult = new ResponseResult();

    public void addPassAssertions() {
        this.passAssertions++;
    }

}
