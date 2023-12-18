package io.metersphere.sdk.dto.api.result;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 实际请求详情
 */
@Data
public class RequestResult {
    // 请求ID
    private String id;

    // 步骤请求唯一ID
    private String resourceId;

    private String threadName;

    private String name;

    private String url;

    private long requestSize;

    private long startTime;

    private long endTime;

    private int error;

    private boolean success;

    private String headers;

    private String cookies;

    private String body;

    private String status;

    private String method;

    private int totalAssertionCount = 0;

    private int passAssertionsCount= 0;

    private List<RequestResult> subRequestResults = new ArrayList<>();

    private ResponseResult responseResult = new ResponseResult();

    public void increasePassAssertionCount() {
        this.passAssertionsCount++;
    }

    private String fakeErrorMessage;
    //  误报编码名称
    private String fakeErrorCode;

}
