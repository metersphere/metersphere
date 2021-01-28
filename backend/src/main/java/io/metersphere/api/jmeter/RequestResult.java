package io.metersphere.api.jmeter;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RequestResult {

    private String name;

    private String url;

    private String method;

    private long requestSize;

    private long startTime;

    private long endTime;

    private int error;

    private boolean success;

    private String headers;

    private String cookies;

    private String body;

    private int totalAssertions = 0;

    private int passAssertions = 0;

    private final List<RequestResult> subRequestResults = new ArrayList<>();

    private final ResponseResult responseResult = new ResponseResult();

    public void addPassAssertions() {
        this.passAssertions++;
    }

}
