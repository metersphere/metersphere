package io.metersphere.sdk.dto.api.result;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 实际请求详情
 */
@Data
public class RequestResult {
    /**
     * 请求ID
     */
    private String resourceId;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 步骤请求唯一ID
     */
    private String stepId;

    /**
     * 线程名称
     */
    private String threadName;

    /**
     * 步骤名称
     */
    private String name;

    /**
     * 请求地址
     */
    private String url;

    /**
     * 请求大小
     */
    private long requestSize;

    /**
     * 请求开始时间
     */
    private long startTime;

    /**
     * 请求结束时间
     */
    private long endTime;

    /**
     * 失败数量
     */
    private int error;

    /**
     * 请求头
     */
    private String headers;

    /**
     * cookies
     */
    private String cookies;

    /**
     * 请求体
     */
    private String body;

    /**
     * 响应状态
     */
    private String status;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 断言总数
     */
    private int assertionTotal = 0;

    /**
     * 断言通过数
     */
    private int passAssertionsTotal = 0;

    /**
     * 子请求结果
     * {@link RequestResult}
     */
    private List<RequestResult> subRequestResults = new ArrayList<>();

    /**
     * 响应结果
     * {@link ResponseResult}
     */
    private ResponseResult responseResult = new ResponseResult();

    /**
     * 是否成功
     */
    private Boolean isSuccessful;

    public void increasePassAssertionCount() {
        this.passAssertionsTotal++;
    }

    /**
     * 误报编码名称
     */
    private String fakeErrorCode;

    /**
     * 前后置脚本执行标识
     */
    private String scriptIdentifier;

}
