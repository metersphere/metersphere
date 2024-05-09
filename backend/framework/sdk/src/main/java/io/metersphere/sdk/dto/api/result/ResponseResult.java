package io.metersphere.sdk.dto.api.result;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 响应结果
 */
@Data
public class ResponseResult {

    /**
     * 响应状态
     */
    private String responseCode;

    /**
     * 响应详细信息
     */
    private String responseMessage;

    /**
     * 总响应时间
     */
    private long responseTime;

    /**
     * 从客户端向服务器发送请求到收到响应的时间
     */
    private long latency;

    /**
     * 总响应内容大小
     */
    private long responseSize;

    /**
     * 响应头
     */
    private String headers;

    /**
     * 响应内容
     */
    private String body;

    /**
     * 响应类型
     */
    private String contentType;

    private List<ExtractResult> extractResults;

    /**
     * 图片内容
     */
    private byte[] imageUrl;

    /**
     * Socket Initialization（Socket 初始化时间）
     */
    private long socketInitTime = 0;

    /**
     * DNS Lookup（DNS 查询时间）
     */
    private long dnsLookupTime = 0;

    /**
     * TCP Handshake（TCP 握手时间）
     */
    private long tcpHandshakeTime = 0;

    /**
     * SSL Handshake（SSL 握手时间）
     */
    private long sslHandshakeTime = 0;

    /**
     * Transfer Start（传输开始时间）
     */
    private long transferStartTime = 0;

    /**
     * Download（下载时间）
     */
    private long downloadTime = 0;

    /**
     * Body size
     */
    private long bodySize = 0;

    /**
     * Header size
     */
    private long headerSize = 0;

    /**
     * 文件地址
     */
    private String filePath;

    /**
     * 断言结果
     */
    private final List<ResponseAssertionResult> assertions = new ArrayList<>();

}
