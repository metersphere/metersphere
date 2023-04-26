package io.metersphere.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RequestResult {
    // 请求ID
    private String id;

    // 步骤请求唯一ID
    private String resourceId;

    private String threadName;

    private String name;

    private String url;

    private String method;

    private String scenario;

    private long requestSize;

    private long startTime;

    private long endTime;

    private int error;

    private boolean success;

    private String headers;

    private String cookies;

    private String body;

    private String status;

    private int totalAssertions = 0;

    private int passAssertions = 0;

    private List<RequestResult> subRequestResults = new ArrayList<>();

    private ResponseResult responseResult = new ResponseResult();

    public void addPassAssertions() {
        this.passAssertions++;
    }

    private String fakeErrorMessage;
    //  误报编码名称
    private String fakeErrorCode;
}
