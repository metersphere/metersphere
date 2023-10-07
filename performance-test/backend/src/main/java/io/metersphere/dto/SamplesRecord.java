package io.metersphere.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SamplesRecord {
    //请求名称 - 错误类型 - 错误请求
    private Map<String, Map<String, List<RequestResult>>> samples;
    private Map<String, Map<String, Long>> sampleCount;
}