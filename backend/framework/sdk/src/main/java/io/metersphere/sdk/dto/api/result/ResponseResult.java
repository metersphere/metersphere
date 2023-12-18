package io.metersphere.sdk.dto.api.result;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 响应结果
 */
@Data
public class ResponseResult {

    private String responseCode;

    private String responseMessage;

    // 总响应时间
    private long responseTime;

    private long latency;

    // 总响应内容大小
    private long responseSize;

    private String headers;

    private String body;

    private String vars;

    private String console;

    private String contentType;

    private byte[] imageUrl;

    // Socket Initialization（Socket 初始化时间）
    private long socketInitTime = 0;

    // DNS Lookup（DNS 查询时间）
    private long dnsLookupTime = 0;

    // TCP Handshake（TCP 握手时间）
    private long tcpHandshakeTime = 0;

    //SSL Handshake（SSL 握手时间）
    private long sslHandshakeTime = 0;

    //Transfer Start（传输开始时间）
    private long transferStartTime = 0;

    //Download（下载时间）
    private long downloadTime = 0;

    // Body size
    private long bodySize = 0;

    // header size
    private long headerSize = 0;

    private final List<ResponseAssertionResult> assertions = new ArrayList<>();

}
